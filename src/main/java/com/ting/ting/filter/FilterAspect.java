package com.ting.ting.filter;

import com.ting.ting.annotation.EnableFilters;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static java.time.LocalDateTime.now;

@Aspect
@Component
public class FilterAspect {

    private final EntityManager entityManager;

    public FilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("@annotation(enableFilters)")
    public void enableFilters(EnableFilters enableFilters) {
        Session session = entityManager.unwrap(Session.class);

        for (FilterType filter : enableFilters.value()) {
            String parameterName = filter.getParameter();
            Object parameterValue = filter.getValue();

            if ("now".equals(parameterValue)) {
                parameterValue = now();
            }
            session.enableFilter(filter.getName()).setParameter(parameterName, parameterValue);
        }
    }

    @After("@annotation(enableFilters)")
    public void disableFilters(EnableFilters enableFilters) {
        Session session = entityManager.unwrap(Session.class);

        for (FilterType filter : enableFilters.value()) {
            session.disableFilter(filter.getName());
        }
    }
}
