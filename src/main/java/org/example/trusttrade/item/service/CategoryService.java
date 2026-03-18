package org.example.trusttrade.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.trusttrade.item.domain.Category;
import org.example.trusttrade.item.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getValidCategories(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(categoryIds);

        if (distinctIds.size() > 3) {
            throw new IllegalArgumentException("카테고리는 최대 3개까지 선택할 수 있습니다.");
        }

        List<Category> categories = categoryRepository.findAllById(distinctIds);

        if (categories.size() != distinctIds.size()) {
            Set<Long> foundIds = categories.stream()
                    .map(Category::getId)
                    .collect(java.util.stream.Collectors.toSet());

            Integer missingId = distinctIds.stream()
                    .filter(id -> !foundIds.contains(id.longValue()))
                    .findFirst()
                    .orElseThrow();

            throw new EntityNotFoundException("카테고리 없음: ID = " + missingId);
        }

        return categories;
    }

    public List<Category> getCategoryList() {
        return categoryRepository.findAll();
    }





}
