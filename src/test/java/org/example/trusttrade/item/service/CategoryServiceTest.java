package org.example.trusttrade.item.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.trusttrade.item.domain.Category;
import org.example.trusttrade.item.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getValidCategoriesRejectsMoreThanThreeCategories() {
        assertThrows(IllegalArgumentException.class,
                () -> categoryService.getValidCategories(List.of(1, 2, 3, 4)));
    }

    @Test
    void getValidCategoriesReturnsDistinctCategories() {
        Category first = Category.builder().id(1L).categoryName("전자기기").build();
        Category second = Category.builder().id(2L).categoryName("생활용품").build();

        when(categoryRepository.findAllById(java.util.Set.of(1, 2)))
                .thenReturn(List.of(first, second));

        List<Category> result = categoryService.getValidCategories(List.of(1, 1, 2));

        assertEquals(2, result.size());
        assertEquals(List.of(first, second), result);
    }

    @Test
    void getValidCategoriesThrowsWhenCategoryIsMissing() {
        Category first = Category.builder().id(1L).categoryName("전자기기").build();

        when(categoryRepository.findAllById(java.util.Set.of(1, 2)))
                .thenReturn(List.of(first));

        assertThrows(EntityNotFoundException.class,
                () -> categoryService.getValidCategories(List.of(1, 2)));
    }
}
