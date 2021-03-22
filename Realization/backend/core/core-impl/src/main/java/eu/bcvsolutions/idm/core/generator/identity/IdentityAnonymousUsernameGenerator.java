package eu.bcvsolutions.idm.core.generator.identity;

import java.math.BigDecimal;
import java.lang.Math;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Implementation of the username generator created from defined prefix and random number sequence.
 * Ensures username uniqueness.  
 *
 * @author Ondrej Husnik
 * @since 11.0.0
 */
@Component(IdentityAnonymousUsernameGenerator.GENERATOR_NAME)
@Description("Generate idenity username from set prefix and generated number sequence.")
public class IdentityAnonymousUsernameGenerator extends AbstractValueGenerator<IdmIdentityDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityAnonymousUsernameGenerator.class);

	public static final String GENERATOR_NAME = "core-identity-anonymous-username-value-generator";
	
	public static final String USERNAME_PREFIX = "usernamePrefix";
	public static final String GENERATED_NUMBER_LENGTH = "generatedNumberLength";
	
	private static final int DEFAULT_GENERATED_NUMBER_LENGTH = 4;
	private static final int MIN_GENERATED_NUMBER_LENGTH = 1;
	private static final int MAX_GENERATED_NUMBER_LENGTH = 9;
	private static final int GENERATE_ATTEMPTS = 5;
	private static final int SEARCH_PAGE_SIZE = 1000; 

	@Autowired
	private IdmIdentityService identityService;
	
	@Override
	public String getName() {
		return GENERATOR_NAME;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = super.getPropertyNames();
		properties.add(USERNAME_PREFIX);
		properties.add(GENERATED_NUMBER_LENGTH);
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.forEach(attribute -> {
			if (attribute.getName().equals(USERNAME_PREFIX)) {
				attribute.setPersistentType(PersistentType.SHORTTEXT);
			} else if (attribute.getName().equals(GENERATED_NUMBER_LENGTH)) {
				attribute.setPersistentType(PersistentType.INT);
				attribute.setDefaultValue(String.valueOf(DEFAULT_GENERATED_NUMBER_LENGTH));
				attribute.setMin(new BigDecimal(MIN_GENERATED_NUMBER_LENGTH));
				attribute.setMax(new BigDecimal(MAX_GENERATED_NUMBER_LENGTH));
			}			
		});
		return attributes;
	}

	@Override
	public IdmIdentityDto generate(IdmIdentityDto dto, IdmGenerateValueDto valueGenerator) {
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(valueGenerator, "Value generator is required.");
		
		// if exists username and configuration doesn't allow regenerate return dto
		if (!valueGenerator.isRegenerateValue() && StringUtils.isNotEmpty(dto.getUsername())) {
			return dto;
		}
		
		// generator configuration
		int numPartLen = getNumberPartLength(valueGenerator);
		int numPartMax = calcMaxValueForLen(numPartLen);
		String prefix = getPrefixString(valueGenerator);
		
		// try to generate the new unique username within several attempts
		int attepts = GENERATE_ATTEMPTS;
		do {
			int randomPartVal = generateNumberPart(numPartMax);
			String username = createUsername(prefix, randomPartVal, numPartLen);
			// find at the first attempt OK
			if (!usernameInUse(username)) {
				dto.setUsername(username);
				return dto;
			}
		} while(--attepts != 0);
		
		// unsuccessful with random generation -> search for empty number slots
		String usernameRoot = createUsername(prefix, null, null);
		IdmIdentityFilter identityFilt = new IdmIdentityFilter();
		identityFilt.setText(usernameRoot);
		Page<IdmIdentityDto> page = null;
		int pageSize = SEARCH_PAGE_SIZE;
		int pageNum = 0;
		do {
			page = identityService.find(identityFilt,
					PageRequest.of(pageNum, pageSize, Sort.by("username").ascending()));
			List<IdmIdentityDto> dtos = page.getContent();
			List<String> usernameNumbers = dtos.stream()
					.map(IdmIdentityDto::getUsername)
					.filter(usernameFilterFactory(usernameRoot, numPartLen))
					.map(name -> name.replace(usernameRoot, ""))
					.collect(Collectors.toList());
			
			Integer newRandomPartVal = findEmptySlotFromRange(usernameNumbers, 0, usernameNumbers.size()-1);
			if (newRandomPartVal != null) {
				String username = createUsername(prefix, newRandomPartVal, Integer.valueOf(numPartLen));
				dto.setUsername(username);
				return dto;
			}
			pageNum++;
		} while(pageNum < page.getTotalPages());

		// unable to find empty space by bisect? try min and max values which may still be missing
		String username = createUsername(prefix, 0, numPartLen); // first index
		if (!usernameInUse(username)) {
			dto.setUsername(username);
			return dto;
		}
		username = createUsername(prefix, numPartMax, numPartLen); // last index
		if (!usernameInUse(username)) {
			dto.setUsername(username);
			return dto;
		}
		//it's over nothing remains to try
		LOG.warn("The anonymous username generator reached the limit of the count of available numbers. Increase nuemric part length in the generator setting.");
		throw new ResultCodeException(CoreResultCode.IDENTITY_UNABLE_GENERATE_UNIQUE_USERNAME);
	}
	
	
	/**
	 * Searches for available numeric part of generated username. 
	 * @param data
	 * @param startIdx
	 * @param endIdx
	 * @return
	 */
	private Integer findEmptySlotFromRange(List<String> data, int startIdx, int endIdx) {
		Integer availableIndex = null;
		int first=0;
		int last=0;
		int sumExpected=0;
		int sumPresent=0;
		
		// nowhere to search
		if (data.isEmpty() || (startIdx > endIdx)) {
			return null;
		}
				
		// get numbers from string and calculate sums
		do {
			try {
				first = Integer.parseUnsignedInt(data.get(startIdx));
				break;
			} catch (NumberFormatException numEx) {
				startIdx++; // if an error occurs while parsing omit that item 
			}
		} while(startIdx < endIdx);
		
		do {
			try {
				last = Integer.parseUnsignedInt(data.get(endIdx));
				break;
			} catch (NumberFormatException numEx) {
				endIdx--; // if an error occurs while parsing omit that item
			}
		} while(startIdx < endIdx); 
		
		sumExpected = calcSumOfContinuousRow(startIdx, endIdx);
		sumPresent = calcSumOfContinuousRow(first, last);
		
		// no gap in number range
		if (sumExpected == sumPresent) {
			return null;
		}
		
		// there are empty slots between those two indices 
		if (endIdx - startIdx < 2) {
			try {
				first = Integer.parseUnsignedInt(data.get(startIdx));
				return first+1; // we found it!!!!!!!!!
			} catch (NumberFormatException numEx) {
				return null;
			}
		}
		
		// divide and conquer - next search iteration within halved interval
		int middleIdx = (endIdx+startIdx)/2;
		availableIndex = findEmptySlotFromRange(data, startIdx, middleIdx);
		if (availableIndex == null) {
			availableIndex = findEmptySlotFromRange(data, middleIdx, endIdx);
		}
		return availableIndex;
	}
	
	
	/**
	 * Creates a stream predicate (function) for filtering irrelevant usernames
	 * which are found by database filter citeria and should be omitted  
	 * @param usernamePrefix
	 * @param length
	 * @return
	 */
	public static Predicate<String> usernameFilterFactory(String usernamePrefix, int length) {
		String prefix = StringUtils.trimToNull(usernamePrefix);
		StringBuilder regExp = new StringBuilder();
		regExp.append("^");
		if (prefix != null) {
			regExp.append(Pattern.quote(prefix));
		}
		regExp.append("[0-9]{");
		regExp.append(length);
		regExp.append("}$");
		return username -> username.matches(regExp.toString());
	}
	
	/**
	 * Method calculating expected sum of numbers in rang.
	 * Used for determining whether there are some free numbers within tested range 
	 * @param startVal
	 * @param endVal
	 * @return
	 */
	private int calcSumOfContinuousRow(int startVal, int endVal) {
		int n = endVal-startVal;
		return (n*(n+1))/2;
	}
	
	/**
	 * Creates username with required format 
	 * @param prefix
	 * @param linker
	 * @param generatedPartVal
	 * @param generatedPartLen
	 * @return
	 */
	public static String createUsername(String prefix, Integer generatedPartVal, Integer generatedPartLen) {
		StringBuilder sb = new StringBuilder();
		if (!prefix.isEmpty()) {
			sb.append(prefix);
		}
		if (generatedPartLen != null && generatedPartVal != null) {
			StringBuilder format = new StringBuilder("%0")
					.append(generatedPartLen)
					.append("d");
			sb.append(String.format(format.toString(), generatedPartVal));
		}
		return sb.toString();
	}
	
	/**
	 * Max value of the number for given number length 
	 * @param len
	 * @return
	 */
	public static int calcMaxValueForLen(int len) {
		return ((int) Math.pow(10d, (double)len) - 1);
	}
	
	/**
	 * Generator of a random value from 0 to max
	 * @param max
	 * @return
	 */
	private int generateNumberPart(int max) {
		return (int)(Math.random() * max);
	}
	
	/**
	 * Test whether user name is free for using 
	 * @param username
	 * @return
	 */
	private boolean usernameInUse(String username) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setUsername(username);
		return identityService.count(filter) > 0;
	}

		
	private String getPrefixString(IdmGenerateValueDto valueGenerator) {
		return StringUtils.stripToEmpty(valueGenerator.getGeneratorProperties().getString(USERNAME_PREFIX));
	}
	
	private int getNumberPartLength(IdmGenerateValueDto valueGenerator) {
		int result = valueGenerator.getGeneratorProperties().getInt(GENERATED_NUMBER_LENGTH);
		if (result < MIN_GENERATED_NUMBER_LENGTH) {
			result = MIN_GENERATED_NUMBER_LENGTH;
		}
		if (result > MAX_GENERATED_NUMBER_LENGTH) {
			result = MAX_GENERATED_NUMBER_LENGTH;
		}
		return result;
	}
}
