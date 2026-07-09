package com.cafepilot.domain.menu.service;

import com.cafepilot.domain.cafe.entity.Cafe;
import com.cafepilot.domain.cafe.exception.CafeException;
import com.cafepilot.domain.cafe.repository.CafeRepository;
import com.cafepilot.domain.menu.dto.CreateMenuRequest;
import com.cafepilot.domain.menu.dto.MenuResponse;
import com.cafepilot.domain.menu.dto.UpdateMenuRequest;
import com.cafepilot.domain.menu.entity.Menu;
import com.cafepilot.domain.menu.exception.MenuException;
import com.cafepilot.domain.menu.repository.MenuRepository;
import com.cafepilot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CafeRepository cafeRepository;

    public List<MenuResponse> getMenus(Long cafeId) {
        return menuRepository.findByCafeIdAndDeletedAtIsNullOrderByDisplayOrderAsc(cafeId)
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional
    public MenuResponse createMenu(Long memberId, Long cafeId, CreateMenuRequest request) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        Menu menu = Menu.create(
                cafeId,
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                request.displayOrder()
        );
        menuRepository.save(menu);
        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse updateMenu(Long memberId, Long cafeId, Long menuId, UpdateMenuRequest request) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        Menu menu = findActiveMenu(menuId);
        menu.update(request.name(), request.description(), request.price(),
                request.category(), request.displayOrder());
        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse toggleAvailable(Long memberId, Long cafeId, Long menuId) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        Menu menu = findActiveMenu(menuId);
        if (menu.isAvailable()) {
            menu.markUnavailable();
        } else {
            menu.markAvailable();
        }
        return MenuResponse.from(menu);
    }

    @Transactional
    public void deleteMenu(Long memberId, Long cafeId, Long menuId) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        Menu menu = findActiveMenu(menuId);
        menu.softDelete();
    }

    private Cafe findActiveCafe(Long cafeId) {
        return cafeRepository.findByIdAndDeletedAtIsNull(cafeId)
                .orElseThrow(() -> new CafeException(ErrorCode.CAFE_NOT_FOUND));
    }

    private Menu findActiveMenu(Long menuId) {
        return menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new MenuException(ErrorCode.MENU_NOT_FOUND));
    }

    private void validateOwner(Cafe cafe, Long memberId) {
        if (!cafe.isOwnedBy(memberId)) {
            throw new CafeException(ErrorCode.CAFE_ACCESS_DENIED);
        }
    }
}
