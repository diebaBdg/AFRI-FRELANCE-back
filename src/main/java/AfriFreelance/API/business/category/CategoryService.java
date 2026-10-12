package AfriFreelance.API.business.category;

import AfriFreelance.API.business.category.dtos.CategoryDTO;
import AfriFreelance.API.business.category.dtos.CategoryRequest;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.business.profile.ProfileService;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategory(UUID id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Catégorie non trouvée", id));
        return toDTO(cat);
    }

    public CategoryDTO createCategory(CategoryRequest request) {
        requireAdmin();
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("DUPLICATE_CATEGORY", "Cette catégorie existe déjà", request.getName());
        }
        Category cat = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconName(request.getIconName())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        cat = categoryRepository.save(cat);
        return toDTO(cat);
    }

    public CategoryDTO updateCategory(UUID id, CategoryRequest request) {
        requireAdmin();
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Catégorie non trouvée", id));

        if (request.getName() != null && !request.getName().equals(cat.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new BusinessException("DUPLICATE_CATEGORY", "Ce nom est déjà utilisé", request.getName());
            }
            cat.setName(request.getName());
        }
        if (request.getDescription() != null) cat.setDescription(request.getDescription());
        if (request.getIconName() != null) cat.setIconName(request.getIconName());
        if (request.getIsActive() != null) cat.setIsActive(request.getIsActive());
        if (request.getDisplayOrder() != null) cat.setDisplayOrder(request.getDisplayOrder());

        cat = categoryRepository.save(cat);
        return toDTO(cat);
    }

    public void deleteCategory(UUID id) {
        requireAdmin();
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Catégorie non trouvée", id));
        categoryRepository.delete(cat);
    }

    private UUID requireAdmin() {
        UUID userId = profileService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getCode()));
        if (!isAdmin) {
            throw new BusinessException("NOT_ADMIN", "Accès réservé aux administrateurs", userId);
        }
        return userId;
    }

    private CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .iconName(c.getIconName())
                .isActive(c.getIsActive())
                .displayOrder(c.getDisplayOrder())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
