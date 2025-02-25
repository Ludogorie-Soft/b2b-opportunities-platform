package com.example.b2b_opportunities.Repository;

import com.example.b2b_opportunities.Entity.Company;
import com.example.b2b_opportunities.Entity.PartnerGroup;
import com.example.b2b_opportunities.Entity.Skill;
import com.example.b2b_opportunities.Entity.SkillExperience;
import com.example.b2b_opportunities.Entity.Talent;
import com.example.b2b_opportunities.Entity.TalentExperience;
import com.example.b2b_opportunities.Entity.WorkMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CustomTalentRepositoryImpl implements CustomTalentRepository {

    private final EntityManager entityManager;

    @Override
    public Page<Talent> findTalentsByFilters(
            Long currentCompanyId,
            Set<Long> workModes,
            Set<Long> skills,
            Integer rate,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Talent> cq = cb.createQuery(Talent.class);
        Root<Talent> root = cq.from(Talent.class);
        Join<Talent, Company> companyJoin = root.join("company");

        if (pageable.getSort().stream().anyMatch(order -> order.getProperty().equalsIgnoreCase("talentExperience.totalTime"))) {
            root.fetch("talentExperience", JoinType.INNER);
        }

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.isTrue(root.get("isActive")));
        predicates.add(cb.notEqual(root.get("company").get("id"), currentCompanyId));

        predicates.add(buildVisibilityPredicate(cb, companyJoin, currentCompanyId));

        if (workModes != null && !workModes.isEmpty()) {
            Join<Talent, WorkMode> workModeJoin = root.join("workModes");
            predicates.add(workModeJoin.get("id").in(workModes));
        }

        if (skills != null && !skills.isEmpty()) {
            Subquery<Long> skillSubquery = cq.subquery(Long.class);
            Root<Talent> talentRoot = skillSubquery.correlate(root);
            Join<Talent, TalentExperience> teJoin = talentRoot.join("talentExperience");
            Join<TalentExperience, SkillExperience> seJoin = teJoin.join("skillExperienceList");
            Join<SkillExperience, Skill> skillJoin = seJoin.join("skill");

            skillSubquery.select(talentRoot.get("id"))
                    .where(skillJoin.get("id").in(skills));
            predicates.add(cb.exists(skillSubquery));
        }

        if (rate != null) {
            predicates.add(cb.and(
                    cb.lessThanOrEqualTo(root.get("minRate"), rate),
                    cb.greaterThanOrEqualTo(root.get("maxRate"), rate)
            ));
        }

        cq.where(predicates.toArray(new Predicate[0])).distinct(true);

        applySorting(cb, cq, root, pageable);

        TypedQuery<Talent> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        Long total = getTotalCount(currentCompanyId, workModes, skills, rate);

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    private Predicate buildVisibilityPredicate(CriteriaBuilder cb, Join<Talent, Company> companyJoin, Long currentCompanyId) {
        Subquery<Long> partnerSubquery = cb.createQuery().subquery(Long.class);
        Root<PartnerGroup> pgRoot = partnerSubquery.from(PartnerGroup.class);
        Join<PartnerGroup, Company> partnersJoin = pgRoot.join("partners");

        partnerSubquery.select(cb.literal(1L))
                .where(
                        cb.equal(pgRoot.get("company"), companyJoin),
                        cb.equal(partnersJoin.get("id"), currentCompanyId)
                );

        return cb.or(
                cb.isTrue(companyJoin.get("talentsSharedPublicly")),
                cb.and(
                        cb.isFalse(companyJoin.get("talentsSharedPublicly")),
                        cb.exists(partnerSubquery)
                )
        );
    }

    private void applySorting(CriteriaBuilder cb, CriteriaQuery<Talent> cq, Root<Talent> root, Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            List<Order> orders = pageable.getSort().stream()
                    .map(order -> {
                        String[] parts = order.getProperty().split("\\.");
                        Path<?> path = root;
                        for (String part : parts) {
                            path = path.get(part);
                        }
                        return order.isAscending() ? cb.asc(path) : cb.desc(path);
                    })
                    .collect(Collectors.toList());
            cq.orderBy(orders);
        }
    }

    private Long getTotalCount(Long currentCompanyId, Set<Long> workModes, Set<Long> skills, Integer rate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Talent> root = cq.from(Talent.class);
        Join<Talent, Company> companyJoin = root.join("company");

        cq.select(cb.countDistinct(root));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("isActive")));
        predicates.add(cb.notEqual(root.get("company").get("id"), currentCompanyId));
        predicates.add(buildVisibilityPredicate(cb, companyJoin, currentCompanyId));

        if (workModes != null && !workModes.isEmpty()) {
            Join<Talent, WorkMode> workModeJoin = root.join("workModes");
            predicates.add(workModeJoin.get("id").in(workModes));
        }

        if (skills != null && !skills.isEmpty()) {
            Subquery<Long> skillSubquery = cq.subquery(Long.class);
            Root<Talent> talentRoot = skillSubquery.correlate(root);
            Join<Talent, TalentExperience> teJoin = talentRoot.join("talentExperience");
            Join<TalentExperience, SkillExperience> seJoin = teJoin.join("skillExperienceList");
            Join<SkillExperience, Skill> skillJoin = seJoin.join("skill");

            skillSubquery.select(talentRoot.get("id"))
                    .where(skillJoin.get("id").in(skills));
            predicates.add(cb.exists(skillSubquery));
        }

        if (rate != null) {
            predicates.add(cb.and(
                    cb.lessThanOrEqualTo(root.get("minRate"), rate),
                    cb.greaterThanOrEqualTo(root.get("maxRate"), rate)
            ));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getSingleResult();
    }
}
