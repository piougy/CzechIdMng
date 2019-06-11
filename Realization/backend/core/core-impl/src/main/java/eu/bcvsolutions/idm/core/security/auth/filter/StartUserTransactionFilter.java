package eu.bcvsolutions.idm.core.security.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import eu.bcvsolutions.idm.core.api.domain.TransactionContext;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;

/**
 * Assing new transaction context to new request
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 * @see TransactionContextHolder
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StartUserTransactionFilter extends OncePerRequestFilter {
	
	private static final Logger LOG = LoggerFactory.getLogger(StartUserTransactionFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		TransactionContext context = TransactionContextHolder.createEmptyContext();
		TransactionContextHolder.setContext(context);
		//
		LOG.trace("Starting new user transaction [{}]", context.getTransactionId());
		//
		chain.doFilter(request, response);
	}
}
