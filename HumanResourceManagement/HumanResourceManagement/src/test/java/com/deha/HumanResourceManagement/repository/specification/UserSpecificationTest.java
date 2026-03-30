package com.deha.HumanResourceManagement.repository.specification;

import com.deha.HumanResourceManagement.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class UserSpecificationTest {

    @Test
    void search_shouldReturnNullPredicateWhenKeywordIsBlank() {
        Root<User> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Specification<User> specification = UserSpecification.search("   ");

        assertNull(specification.toPredicate(root, query, cb));
        verifyNoInteractions(root, cb);
    }

    @Test
    void search_shouldNormalizeKeywordAndApplyCaseInsensitiveLike() {
        Root<User> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> firstNamePath = mock(Path.class);
        Path<Object> lastNamePath = mock(Path.class);
        Path<Object> emailPath = mock(Path.class);
        Path<Object> phonePath = mock(Path.class);

        Expression<String> firstNameExpression = mock(Expression.class);
        Expression<String> lastNameExpression = mock(Expression.class);
        Expression<String> emailExpression = mock(Expression.class);
        Expression<String> phoneExpression = mock(Expression.class);

        Expression<String> lowerFirstName = mock(Expression.class);
        Expression<String> lowerLastName = mock(Expression.class);
        Expression<String> lowerEmail = mock(Expression.class);
        Expression<String> lowerPhone = mock(Expression.class);

        Predicate firstNamePredicate = mock(Predicate.class);
        Predicate lastNamePredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);
        Predicate phonePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.get("firstName")).thenReturn(firstNamePath);
        when(root.get("lastName")).thenReturn(lastNamePath);
        when(root.get("email")).thenReturn(emailPath);
        when(root.get("phone")).thenReturn(phonePath);

        when(firstNamePath.as(String.class)).thenReturn(firstNameExpression);
        when(lastNamePath.as(String.class)).thenReturn(lastNameExpression);
        when(emailPath.as(String.class)).thenReturn(emailExpression);
        when(phonePath.as(String.class)).thenReturn(phoneExpression);

        when(cb.lower(firstNameExpression)).thenReturn(lowerFirstName);
        when(cb.lower(lastNameExpression)).thenReturn(lowerLastName);
        when(cb.lower(emailExpression)).thenReturn(lowerEmail);
        when(cb.lower(phoneExpression)).thenReturn(lowerPhone);

        when(cb.like(lowerFirstName, "%john%")) .thenReturn(firstNamePredicate);
        when(cb.like(lowerLastName, "%john%")) .thenReturn(lastNamePredicate);
        when(cb.like(lowerEmail, "%john%")) .thenReturn(emailPredicate);
        when(cb.like(lowerPhone, "%john%")) .thenReturn(phonePredicate);
        when(cb.or(firstNamePredicate, lastNamePredicate, emailPredicate, phonePredicate)).thenReturn(combinedPredicate);

        Specification<User> specification = UserSpecification.search("  JoHn  ");
        specification.toPredicate(root, query, cb);

        verify(cb).like(lowerFirstName, "%john%");
        verify(cb).like(lowerLastName, "%john%");
        verify(cb).like(lowerEmail, "%john%");
        verify(cb).like(lowerPhone, "%john%");
        verify(cb).or(firstNamePredicate, lastNamePredicate, emailPredicate, phonePredicate);
    }
}

