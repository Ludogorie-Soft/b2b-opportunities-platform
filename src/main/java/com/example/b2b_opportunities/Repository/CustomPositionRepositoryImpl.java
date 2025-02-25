package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Position;
import com.example.b2b_opportunities.Entity.Project;
import com.example.b2b_opportunities.Entity.RequiredSkill;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Static.ProjectStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class CustomPositionRepositoryImpl implements CustomPositionRepository {

    private final EntityManager entityManager;

    @Override
    public Page<Position> findPositionsByFilters(
            Boolean isPartnerOnly,
            Long companyId,
            ProjectStatus projectStatus,
            Integer rate,
            Set<Long> workModes,
            Set<Long> skills,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery(); // Променете на Tuple
        Root<Position> root = cq.from(Position.class);

        Join<Position, Project> projectJoin = fetchJoins(root);

        Predicate predicate = buildPredicates(cb, root, projectJoin, isPartnerOnly, companyId, projectStatus, rate, workModes, skills);

        cq.where(predicate).distinct(true);

        // Добавете всички необходими полета в SELECT
        cq.multiselect(
                root.alias("position"), // Основният ентити
                root.get("rate").get("min").alias("rateMin"), // rate.min
                root.get("rate").get("max").alias("rateMax")  // rate.max
        );

        // Прилагане на сортирането
        if (pageable.getSort().isSorted()) {
            List<Order> orders = pageable.getSort().stream()
                    .flatMap(order -> {
                        String property = order.getProperty();
                        if ("rate".equals(property)) {
                            Path<Integer> rateMinPath = root.get("rate").get("min");
                            Path<Integer> rateMaxPath = root.get("rate").get("max");
                            Order minOrder = order.isAscending() ? cb.asc(rateMinPath) : cb.desc(rateMinPath);
                            Order maxOrder = order.isAscending() ? cb.asc(rateMaxPath) : cb.desc(rateMaxPath);
                            return Stream.of(minOrder, maxOrder);
                        } else {
                            String[] attributePath = property.split("\\.");
                            Path<?> path = root;
                            for (String attribute : attributePath) {
                                path = path.get(attribute);
                            }
                            Order orderObj = order.isAscending() ? cb.asc(path) : cb.desc(path);
                            return Stream.of(orderObj);
                        }
                    })
                    .collect(Collectors.toList());
            cq.orderBy(orders);
        }

        TypedQuery<Tuple> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // Преобразувайте Tuple към Position
        List<Position> positions = query.getResultList().stream()
                .map(tuple -> {
                    Position position = tuple.get("position", Position.class);
                    Integer rateMin = tuple.get("rateMin", Integer.class);
                    Integer rateMax = tuple.get("rateMax", Integer.class);
                    // Ако е необходимо, можете да сетнете rateMin и rateMax в Position
                    return position;
                })
                .collect(Collectors.toList());

        // Get total count
        Long total = getTotalCount(isPartnerOnly, companyId, projectStatus, rate, workModes, skills);

        return new PageImpl<>(positions, pageable, total);
    }

    private Join<Position, Project> fetchJoins(Root<Position> root) {
        Fetch<Position, Project> projectFetch = root.fetch("project", JoinType.INNER);
        Join<Position, Project> projectJoin = (Join<Position, Project>) projectFetch;

        Fetch<Project, PartnerGroup> partnerGroupFetch = projectJoin.fetch("partnerGroupList", JoinType.LEFT);
        Join<Project, PartnerGroup> partnerGroupJoin = (Join<Project, PartnerGroup>) partnerGroupFetch;
        partnerGroupJoin.fetch("partners", JoinType.LEFT);

        root.join("requiredSkills", JoinType.LEFT).join("skill", JoinType.LEFT);
        root.fetch("workModes", JoinType.LEFT);

        return projectJoin;
    }

    private Predicate buildPredicates(
            CriteriaBuilder cb,
            Root<Position> root,
            Join<Position, Project> projectJoin,
            Boolean isPartnerOnly,
            Long companyId,
            ProjectStatus projectStatus,
            Integer rate,
            Set<Long> workModes,
            Set<Long> skills) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(projectJoin.get("projectStatus"), projectStatus));

        predicates.add(ratePredicate(cb, root, rate));

        predicates.add(collectionPredicate(cb, root, workModes));

        Join<Position, RequiredSkill> requiredSkillJoin = root.join("requiredSkills", JoinType.LEFT);
        Join<RequiredSkill, Skill> skillJoin = requiredSkillJoin.join("skill", JoinType.LEFT);

        if (skills != null && !skills.isEmpty()) {
            predicates.add(skillJoin.get("id").in(skills));
        }


        // isPartnerOnly condition
        predicates.add(partnerOnlyPredicate(cb, projectJoin, companyId, isPartnerOnly));

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate ratePredicate(CriteriaBuilder cb, Root<Position> root, Integer rate) {
        if (rate == null) {
            return cb.conjunction();
        }
        return cb.and(
                cb.lessThanOrEqualTo(root.get("rate").get("min"), rate),
                cb.greaterThanOrEqualTo(root.get("rate").get("max"), rate)
        );
    }

    private Predicate collectionPredicate(
            CriteriaBuilder cb,
            Root<Position> root,
            Set<Long> values) {

        if (values == null || values.isEmpty()) {
            return cb.conjunction();
        }

        Join<Position, ?> join = root.join("workModes", JoinType.LEFT);
        return join.get("id").in(values);
    }

    private Predicate partnerOnlyPredicate(
            CriteriaBuilder cb,
            Join<Position, Project> projectJoin,
            Long companyId,
            Boolean isPartnerOnly) {

        Join<Project, PartnerGroup> partnerGroupJoin = projectJoin.join("partnerGroupList", JoinType.LEFT);
        Join<PartnerGroup, Company> companyJoin = partnerGroupJoin.join("partners", JoinType.LEFT);

        List<Predicate> partnerPredicates = new ArrayList<>();

        if (isPartnerOnly == null) {
            Predicate nonPartner = cb.equal(projectJoin.get("isPartnerOnly"), false);
            Predicate partner = cb.and(
                    cb.equal(projectJoin.get("isPartnerOnly"), true),
                    cb.equal(companyJoin.get("id"), companyId)
            );
            partnerPredicates.add(cb.or(nonPartner, partner));
        } else if (isPartnerOnly) {
            partnerPredicates.add(cb.equal(projectJoin.get("isPartnerOnly"), true));
            partnerPredicates.add(cb.equal(companyJoin.get("id"), companyId));
        } else {
            partnerPredicates.add(cb.equal(projectJoin.get("isPartnerOnly"), false));
        }

        return cb.and(partnerPredicates.toArray(new Predicate[0]));
    }

    private void applySorting(CriteriaBuilder cb, CriteriaQuery<Position> cq, Root<Position> root, Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            List<Order> orders = pageable.getSort().stream()
                    .flatMap(order -> {
                        String property = order.getProperty();
                        if ("rate".equals(property)) {
                            // Създаване на пътища за rate.min и rate.max
                            Path<Integer> rateMinPath = root.get("rate").get("min");
                            Path<Integer> rateMaxPath = root.get("rate").get("max");

                            // Добавяне на rate.min и rate.max в SELECT клаузата
                            cq.multiselect(root, rateMinPath, rateMaxPath);

                            // Създаване на Order обекти за min и max
                            Order minOrder = order.isAscending() ? cb.asc(rateMinPath) : cb.desc(rateMinPath);
                            Order maxOrder = order.isAscending() ? cb.asc(rateMaxPath) : cb.desc(rateMaxPath);

                            return Stream.of(minOrder, maxOrder);
                        } else {
                            // Обработка на другите полета както преди
                            String[] attributePath = property.split("\\.");
                            Path<?> path = root;
                            for (String attribute : attributePath) {
                                path = path.get(attribute);
                            }
                            Order orderObj = order.isAscending() ? cb.asc(path) : cb.desc(path);
                            return Stream.of(orderObj);
                        }
                    })
                    .collect(Collectors.toList());
            cq.orderBy(orders);
        }
    }

    private Long getTotalCount(
            Boolean isPartnerOnly,
            Long companyId,
            ProjectStatus projectStatus,
            Integer rate,
            Set<Long> workModes,
            Set<Long> skills) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Position> root = cq.from(Position.class);

        cq.select(cb.countDistinct(root));

        Join<Position, Project> projectJoin = root.join("project", JoinType.INNER);

        Predicate predicate = buildPredicates(cb, root, projectJoin, isPartnerOnly, companyId, projectStatus, rate, workModes, skills);
        cq.where(predicate);

        return entityManager.createQuery(cq).getSingleResult();
    }
}