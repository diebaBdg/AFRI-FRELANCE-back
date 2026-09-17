package AfriFreelance.API.business.company;

import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.CompanyRole;
import AfriFreelance.API.enums.CompanySize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public CompanyDTO getMyCompany() {
        UUID userId = profileService.getCurrentUserId();
        List<Company> companies = companyRepository.findByOwnerId(userId);
        if (companies.isEmpty()) {
            throw new BusinessException("NO_COMPANY", "Vous n'avez pas d'entreprise", userId);
        }
        return toDTO(companies.get(0));
    }

    public CompanyDTO createCompany(CompanyRequest request) {
        UUID userId = profileService.getCurrentUserId();
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        Company company = Company.builder()
                .ownerId(userId)
                .commercialName(request.getCommercialName())
                .legalName(request.getLegalName())
                .sector(request.getSector())
                .size(request.getSize() != null ? CompanySize.valueOf(request.getSize()) : CompanySize.SOLO)
                .country(request.getCountry())
                .city(request.getCity())
                .website(request.getWebsite())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .isVerified(false)
                .build();
        company = companyRepository.save(company);

        CompanyMember ownerMember = CompanyMember.builder()
                .company(company)
                .user(owner)
                .role(CompanyRole.OWNER)
                .build();
        companyMemberRepository.save(ownerMember);

        return toDTO(company);
    }

    public CompanyDTO updateCompany(UUID companyId, CompanyRequest request) {
        UUID userId = profileService.getCurrentUserId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("COMPANY_NOT_FOUND", "Entreprise non trouvée", companyId));

        if (!company.getOwnerId().equals(userId)) {
            throw new BusinessException("NOT_COMPANY_OWNER", "Vous n'êtes pas propriétaire de cette entreprise", userId);
        }

        if (request.getCommercialName() != null) company.setCommercialName(request.getCommercialName());
        if (request.getLegalName() != null) company.setLegalName(request.getLegalName());
        if (request.getSector() != null) company.setSector(request.getSector());
        if (request.getSize() != null) {
            try { company.setSize(CompanySize.valueOf(request.getSize())); } catch (IllegalArgumentException ignored) {}
        }
        if (request.getCountry() != null) company.setCountry(request.getCountry());
        if (request.getCity() != null) company.setCity(request.getCity());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getDescription() != null) company.setDescription(request.getDescription());
        if (request.getLogoUrl() != null) company.setLogoUrl(request.getLogoUrl());

        companyRepository.save(company);
        return toDTO(company);
    }

    public void deleteCompany(UUID companyId) {
        UUID userId = profileService.getCurrentUserId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("COMPANY_NOT_FOUND", "Entreprise non trouvée", companyId));

        if (!company.getOwnerId().equals(userId)) {
            throw new BusinessException("NOT_COMPANY_OWNER", "Vous n'êtes pas propriétaire de cette entreprise", userId);
        }

        companyRepository.delete(company);
    }

    public List<CompanyMemberDTO> getMembers(UUID companyId) {
        UUID userId = profileService.getCurrentUserId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("COMPANY_NOT_FOUND", "Entreprise non trouvée", companyId));

        if (!company.getOwnerId().equals(userId)) {
            throw new BusinessException("NOT_COMPANY_OWNER", "Accès refusé", userId);
        }

        return companyMemberRepository.findByCompanyId(companyId).stream()
                .map(this::toMemberDTO)
                .collect(Collectors.toList());
    }

    public List<CompanyMemberDTO> addMember(UUID companyId, AddMemberRequest request) {
        UUID userId = profileService.getCurrentUserId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("COMPANY_NOT_FOUND", "Entreprise non trouvée", companyId));

        if (!company.getOwnerId().equals(userId)) {
            throw new BusinessException("NOT_COMPANY_OWNER", "Accès refusé", userId);
        }

        User newMember = userRepository.findByEmail(request.getIdentifier())
                .orElse(userRepository.findByUsername(request.getIdentifier())
                        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                                "Utilisateur non trouvé: " + request.getIdentifier(), request.getIdentifier())));

        if (companyMemberRepository.findByCompanyIdAndUserId(companyId, newMember.getId()).isPresent()) {
            throw new BusinessException("ALREADY_MEMBER", "Cet utilisateur est déjà membre", newMember.getId());
        }

        CompanyRole role = request.getRole() != null ? CompanyRole.valueOf(request.getRole()) : CompanyRole.MEMBER;

        CompanyMember member = CompanyMember.builder()
                .company(company)
                .user(newMember)
                .role(role)
                .build();
        companyMemberRepository.save(member);

        return getMembers(companyId);
    }

    public List<CompanyMemberDTO> updateMemberRole(UUID companyId, UUID memberId, String role) {
        UUID userId = profileService.getCurrentUserId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("COMPANY_NOT_FOUND", "Entreprise non trouvée", companyId));

        if (!company.getOwnerId().equals(userId)) {
            throw new BusinessException("NOT_COMPANY_OWNER", "Accès refusé", userId);
        }

        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("MEMBER_NOT_FOUND", "Membre non trouvé", memberId));

        member.setRole(CompanyRole.valueOf(role));
        companyMemberRepository.save(member);
        return getMembers(companyId);
    }

    public void removeMember(UUID companyId, UUID memberId) {
        UUID userId = profileService.getCurrentUserId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("COMPANY_NOT_FOUND", "Entreprise non trouvée", companyId));

        if (!company.getOwnerId().equals(userId)) {
            throw new BusinessException("NOT_COMPANY_OWNER", "Accès refusé", userId);
        }

        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("MEMBER_NOT_FOUND", "Membre non trouvé", memberId));

        if (member.getRole() == CompanyRole.OWNER) {
            throw new BusinessException("CANNOT_REMOVE_OWNER", "Vous ne pouvez pas retirer le propriétaire", null);
        }

        companyMemberRepository.delete(member);
    }

    private CompanyDTO toDTO(Company company) {
        List<CompanyMemberDTO> members = companyMemberRepository.findByCompanyId(company.getId()).stream()
                .map(this::toMemberDTO)
                .collect(Collectors.toList());

        return CompanyDTO.builder()
                .id(company.getId())
                .legalName(company.getLegalName())
                .commercialName(company.getCommercialName())
                .sector(company.getSector())
                .size(company.getSize() != null ? company.getSize().name() : "SOLO")
                .country(company.getCountry())
                .city(company.getCity())
                .website(company.getWebsite())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .isVerified(company.getIsVerified())
                .createdAt(company.getCreatedAt())
                .members(members)
                .build();
    }

    private CompanyMemberDTO toMemberDTO(CompanyMember m) {
        return CompanyMemberDTO.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .fullName(m.getUser().getFullName())
                .avatarUrl(m.getUser().getAvatarUrl())
                .role(m.getRole().name())
                .joinedAt(m.getCreatedAt())
                .build();
    }
}
