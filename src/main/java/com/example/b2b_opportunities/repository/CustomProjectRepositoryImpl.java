package com.example.b2b_opportunities.repository;

import com.example.b2b_opportunities.entity.Project;
import com.example.b2b_opportunities.enums.ProjectStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CustomProjectRepositoryImpl implements CustomProjectRepository {

    private final EntityManager entityManager;

    @Override
    public Page<Project> findCompanyProjectsByFilters(
            Long companyId,
            Long userCompanyId,
            boolean ownProjects,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> root = cq.from(Project.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("company").get("id"), companyId));
        predicates.add(cb.equal(root.get("projectStatus"), ProjectStatus.ACTIVE));

        cq.where(predicates.toArray(new Predicate[0]));

        if (pageable.getSort().isSorted()) {
            List<Order> orders = pageable.getSort().stream()
                    .map(order -> {
                        Path<Object> path = root.get(order.getProperty());
                        return order.isAscending() ? cb.asc(path) : cb.desc(path);
                    })
                    .collect(Collectors.toList());
            cq.orderBy(orders);
        }

        TypedQuery<Project> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Project> resultList = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Project> countRoot = countQuery.from(Project.class);
        countQuery.select(cb.countDistinct(countRoot));
        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countRoot.get("company").get("id"), companyId));
        countPredicates.add(cb.equal(countRoot.get("projectStatus"), ProjectStatus.ACTIVE));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }
}