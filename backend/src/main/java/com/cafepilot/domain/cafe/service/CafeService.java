package com.cafepilot.domain.cafe.service;

import com.cafepilot.domain.cafe.dto.CafeResponse;
import com.cafepilot.domain.cafe.dto.CreateCafeRequest;
import com.cafepilot.domain.cafe.dto.UpdateCafeRequest;
import com.cafepilot.domain.cafe.entity.Cafe;
import com.cafepilot.domain.cafe.exception.CafeException;
import com.cafepilot.domain.cafe.repository.CafeRepository;
import com.cafepilot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CafeService {

    private final CafeRepository cafeRepository;

    public CafeResponse getMyCafe(Long memberId) {
        Cafe cafe = findActiveCafeByOwner(memberId);
        return CafeResponse.from(cafe);
    }

    @Transactional
    public CafeResponse createCafe(Long memberId, CreateCafeRequest request) {
        if (cafeRepository.existsByOwnerIdAndDeletedAtIsNull(memberId)) {
            throw new CafeException(ErrorCode.CAFE_ACCESS_DENIED);
        }

        Cafe cafe = Cafe.create(memberId, request.name(), request.address(), request.phone());
        cafeRepository.save(cafe);
        return CafeResponse.from(cafe);
    }

    @Transactional
    public CafeResponse updateCafe(Long memberId, Long cafeId, UpdateCafeRequest request) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        cafe.update(request.name(), request.address(), request.phone());
        return CafeResponse.from(cafe);
    }

    @Transactional
    public CafeResponse toggleOpen(Long memberId, Long cafeId) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        if (cafe.isOpen()) {
            cafe.close();
        } else {
            cafe.open();
        }
        return CafeResponse.from(cafe);
    }

    @Transactional
    public void deleteCafe(Long memberId, Long cafeId) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);
        cafe.softDelete();
    }

    private Cafe findActiveCafe(Long cafeId) {
        return cafeRepository.findByIdAndDeletedAtIsNull(cafeId)
                .orElseThrow(() -> new CafeException(ErrorCode.CAFE_NOT_FOUND));
    }

    private Cafe findActiveCafeByOwner(Long ownerId) {
        return cafeRepository.findByOwnerIdAndDeletedAtIsNull(ownerId)
                .orElseThrow(() -> new CafeException(ErrorCode.CAFE_NOT_FOUND));
    }

    private void validateOwner(Cafe cafe, Long memberId) {
        if (!cafe.isOwnedBy(memberId)) {
            throw new CafeException(ErrorCode.CAFE_ACCESS_DENIED);
        }
    }
}
