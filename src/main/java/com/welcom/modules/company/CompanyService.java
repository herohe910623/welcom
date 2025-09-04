package com.welcom.modules.company;

import com.welcom.modules.account.Account;
import com.welcom.modules.tag.Tag;
import com.welcom.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    public Company createNewCompany(Company company, Account account) {
        Company newCompany = companyRepository.save(company);
        newCompany.addManager(account);
        return newCompany;
    }

    public Company getCompanyToUpdate(Account account, String path) {
        Company company = this.getCompany(path);
        checkIfManager(account, company);
        return company;
    }

    public Company getCompany(String path) {
        Company company = companyRepository.findByPath(path);
        if (company == null) {
            throw new IllegalArgumentException(path + "에 해당하는 회사가 없습니다.");
        }
        return company;
    }

    public void updateCompanyDescription(Company company, CompanyDescriptionForm companyDescriptionForm) {
        modelMapper.map(companyDescriptionForm, company);
    }

    public void updateCompanyImage(Company company, String image) {
        company.setImage(image);
    }

    public void enableCompanyBanner(Company company) {
        company.setUseBanner(true);
    }

    public void disableCompanyBanner(Company company) {
        company.setUseBanner(false);
    }

    public Set<Tag> getTags(Company company) {
        Optional<Company> byId = companyRepository.findById(company.getId());
        return byId.orElseThrow().getTags();
    }

    public void addTags(Company company, Tag tag) {
        company.getTags().add(tag);
    }

    public void removeTags(Company company, Tag tag) {
        company.getTags().remove(tag);
    }

    public Set<Zone> getZones(Company company) {
        Optional<Company> byId = companyRepository.findById(company.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZones(Company company, Zone zone) {
        company.getZones().add(zone);
    }

    public void removeZones(Company company, Zone zone) {
        company.getZones().remove(zone);
    }

    public Company getCompanyToUpdateTag(Account account, String path) {
        Company company = companyRepository.findCompanyWithTagsByPath(path);
        checkIfExistingCompany(path, company);
        checkIfManager(account, company);
        return company;
    }

    public Company getCompanyToUpdateZone(Account account, String path) {
        Company company = companyRepository.findCompanyWithZonesByPath(path);
        checkIfExistingCompany(path, company);
        checkIfManager(account, company);
        return company;
    }

    private void checkIfExistingCompany(String path, Company company) {
        if (company == null) {
            throw new IllegalArgumentException(path + "에 해당하는 회사가 없습니다.");
        }
    }

    private void checkIfManager(Account account, Company company) {
        if (!company.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할수 없습니다.");
        }
    }

    public Company getCompanyToUpdateStatus(Account account, String path) {
        Company company = companyRepository.findCompanyWithManagersByPath(path);
        checkIfExistingCompany(path, company);
        checkIfManager(account, company);
        return company;
    }

    public void publish(Company company) {
        company.publish();
    }

    public void close(Company company) {
        company.close();
    }

    public void startRecruit(Company company) {
        company.startRecruit();

    }

    public void stopRecruit(Company company) {
        company.stopRecruit();
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(CompanyForm.VALID_PATH_PATTERN)) {
            return false;
        }
        return !companyRepository.existsByPath(newPath);
    }

    public void updateCompanyPath(Company company, String newPath) {
        company.setPath(newPath);
    }

    public boolean isValidName(String newName) {
        return newName.length() <= 50;
    }

    public void updateCompanyName(Company company, String newName) {
        company.setName(newName);
    }

    public void removeCompany(Company company) {
       if (company.isRemovable()) {
           companyRepository.delete(company);
       }else {
           throw new IllegalArgumentException("회사를 삭제할 수 없습니다.");
       }
    }

    public void addMember(Company company, Account account) {
        company.addMember(account);
    }

    public void removeMember(Company company, Account account) {
        company.removeMember(account);
    }

    public Company getCompanyToEnroll(String path) {
        Company company = companyRepository.findCompanyOnlyByPath(path);
        checkIfExistingCompany(path, company);
        return company;
    }
}
