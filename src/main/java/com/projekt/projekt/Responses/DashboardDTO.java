package com.projekt.projekt.Responses;

import com.projekt.projekt.Models.Ecommerce.Order;
import com.projekt.projekt.Models.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Data
public class DashboardDTO {
    private Integer totalUsers;
    private Integer totalProducts;
    private Integer totalCompletedOrders;

    private List<Integer> last7daysOrders;
    private List<Integer> last7daysUsers;

    public DashboardDTO() {
    }

    public DashboardDTO(Integer totalUsers, Integer totalProducts, Integer totalCompletedOrders, List<Integer> last7daysOrders,List<Integer> last7daysUsers) {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.totalCompletedOrders = totalCompletedOrders;
        this.last7daysOrders = last7daysOrders;
        this.last7daysUsers = last7daysUsers;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Integer getTotalCompletedOrders() {
        return totalCompletedOrders;
    }

    public void setTotalCompletedOrders(Integer totalCompletedOrders) {
        this.totalCompletedOrders = totalCompletedOrders;
    }

    public List<Integer> getLast7daysOrders() {
        return last7daysOrders;
    }

    public void setLast7daysOrders(List<Integer> last7daysOrders) {
        this.last7daysOrders = last7daysOrders;
    }

    public List<Integer> getLast7daysUsers() {
        return last7daysUsers;
    }

    public void setLast7daysUsers(List<Integer> last7daysUsers) {
        this.last7daysUsers = last7daysUsers;
    }
}

