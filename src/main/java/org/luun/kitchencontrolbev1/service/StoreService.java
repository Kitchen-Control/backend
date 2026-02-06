package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.StoreRequest;
import org.luun.kitchencontrolbev1.dto.response.StoreResponse;
import org.luun.kitchencontrolbev1.entity.Store;

import java.util.List;

public interface StoreService {
    List<StoreResponse> getAllStores();
    StoreResponse getStoreById(Integer id);
    StoreResponse createStore(StoreRequest store);
    StoreResponse updateStore(Integer id, Store updatedStore);
    void deleteStore(Integer id);
}
