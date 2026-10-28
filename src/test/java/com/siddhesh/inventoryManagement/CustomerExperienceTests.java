package com.siddhesh.inventoryManagement;

import com.siddhesh.inventoryManagement.domain.dtos.*;
import com.siddhesh.inventoryManagement.domain.entities.*;
import com.siddhesh.inventoryManagement.domain.mapper.OrderMapper;
import com.siddhesh.inventoryManagement.repositories.*;
import com.siddhesh.inventoryManagement.security.CurrentUser;
import com.siddhesh.inventoryManagement.services.AddressService;
import com.siddhesh.inventoryManagement.services.impl.OrderServiceImpl;
import com.siddhesh.inventoryManagement.services.impl.UserServiceImpl;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerExperienceTests {
    @Test
    void customerHistoryQueriesOnlyTheAuthenticatedOwner() {
        OrderRepository orders = mock(OrderRepository.class);
        User caller = User.builder().id(UUID.randomUUID()).name("Aarav").phoneNumber("9999999999").role(Role.USER).build();
        Order order = Order.builder().id(UUID.randomUUID()).user(caller).build();
        when(orders.findByUser_IdOrderByCreatedAtDescIdDesc(caller.getId())).thenReturn(List.of(order));
        OrderServiceImpl service = new OrderServiceImpl(orders, mock(OrderIdempotencyRepository.class), mock(UserRepository.class), mock(ProductRepository.class), mock(StockTransactionRepository.class), new OrderMapper(), mock(PaymentRepository.class));
        var result = service.listOwnOrders(caller);
        assertEquals("Aarav", result.get(0).getUserName());
        assertEquals(caller.getId(), result.get(0).getUserId());
        verify(orders, never()).findAll();
    }

    @Test
    void addressEditAndDeleteRejectAnotherOwnersAddress() {
        AddressRepository addresses = mock(AddressRepository.class);
        UserRepository users = mock(UserRepository.class);
        UUID caller = UUID.randomUUID(), id = UUID.randomUUID();
        when(users.findByIdForUpdate(caller)).thenReturn(Optional.of(User.builder().id(caller).build()));
        when(addresses.findById(id)).thenReturn(Optional.of(Address.builder().id(id).user(User.builder().id(UUID.randomUUID()).build()).build()));
        AddressService service = new AddressService(addresses, users);
        AddressRequest request = new AddressRequest("12 Park Road", "", "Pune", "MH", "411001", "India", true);
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.save(caller, id, request)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.delete(caller, id)).getStatusCode().value());
        verify(addresses, never()).save(any()); verify(addresses, never()).delete(any());
    }

    @Test
    void defaultAddressReplacesOnlyTheOwnersPreviousDefault() {
        AddressRepository addresses = mock(AddressRepository.class);
        UserRepository users = mock(UserRepository.class);
        User owner = User.builder().id(UUID.randomUUID()).build();
        Address old = Address.builder().id(UUID.randomUUID()).user(owner).isDefault(true).build();
        when(users.findByIdForUpdate(owner.getId())).thenReturn(Optional.of(owner));
        when(addresses.findByUser_IdOrderByIsDefaultDescCreatedAtDesc(owner.getId())).thenReturn(List.of(old));
        when(addresses.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = new AddressService(addresses, users).save(owner.getId(), null, new AddressRequest("12 Park Road", "", "Pune", "MH", "411001", "India", true));
        assertFalse(old.getIsDefault()); assertTrue(result.isDefault());
        verify(users).findByIdForUpdate(owner.getId());
    }

    @Test
    void deletingMemberWithBusinessHistoryDoesNotCascadeDeleteOrders() {
        UserRepository users = mock(UserRepository.class);
        OrderRepository orders = mock(OrderRepository.class);
        UUID id = UUID.randomUUID();
        when(orders.existsByUser_Id(id)).thenReturn(true);
        UserServiceImpl service = new UserServiceImpl(users, orders, mock(StockTransactionRepository.class), mock(AddressRepository.class), mock(CurrentUser.class));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.deleteProfileById(id)).getStatusCode().value());
        verify(users, never()).deleteById(any());
    }

    @Test
    void administratorCannotDemoteTheirOwnAccount() {
        CurrentUser current = mock(CurrentUser.class);
        UUID id = UUID.randomUUID();
        when(current.requireUser()).thenReturn(User.builder().id(id).role(Role.ADMIN).build());
        UserServiceImpl service = new UserServiceImpl(mock(UserRepository.class), mock(OrderRepository.class), mock(StockTransactionRepository.class), mock(AddressRepository.class), current);
        AdminUserUpdateRequest request = new AdminUserUpdateRequest(null, "9999999999", "Admin", Role.USER);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.updateProfileById(id, request)).getStatusCode().value());
    }
}
