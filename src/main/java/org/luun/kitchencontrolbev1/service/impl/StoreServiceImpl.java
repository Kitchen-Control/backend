package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.StoreRequest;
import org.luun.kitchencontrolbev1.dto.response.StoreResponse;
import org.luun.kitchencontrolbev1.entity.Store;
import org.luun.kitchencontrolbev1.repository.StoreRepository;
import org.luun.kitchencontrolbev1.service.StoreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    @Override
    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StoreResponse getStoreById(Integer id) {
        Store store = storeRepository.findById(id).orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
        return mapToResponse(store);
    }

    @Override
    public StoreResponse createStore(StoreRequest request) {
        Store store = new Store();
        store.setStoreName(request.getStoreName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());

        store = storeRepository.save(store);
        return mapToResponse(store);
    }

    @Override
    public StoreResponse updateStore(Integer id, Store request) {
        Store store = storeRepository.findById(id).orElseThrow(() -> new RuntimeException("Store not found with id: " + id));

        store.setStoreName(request.getStoreName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store = storeRepository.save(store);
        return mapToResponse(store);
    }

    @Override
    public void deleteStore(Integer id) {
        storeRepository.deleteById(id);
    }

    private StoreResponse mapToResponse(Store store) {
        return StoreResponse.builder()
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .phone(store.getPhone())
                .address(store.getAddress())
                .build();
    }
}
