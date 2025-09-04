package com.welcom.modules.company;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByPath(String path);

    @EntityGraph(value = "Company.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Company findByPath(String path);

    @EntityGraph(value = "Company.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Company findCompanyWithTagsByPath(String path);

    @EntityGraph(value = "Company.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Company findCompanyWithZonesByPath(String path);

    @EntityGraph(value= "Company.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Company findCompanyWithManagersByPath(String path);

    @EntityGraph(value = "Company.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Company findCompanyWithMembersByPath(String path);

    Company findCompanyOnlyByPath(String path);
}
