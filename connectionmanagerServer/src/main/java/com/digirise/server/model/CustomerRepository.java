package com.digirise.server.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-04
 * Author: shrinkhlak
 */

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    @Query("select cust from Customer cust where cust.name = :name")
    public Stream<Customer> findCustomersByName(@Param("name") String name);


    //Not used - just for tests
    @Query("select cust from Customer cust where cust.customerId = :customer_id")
    public Stream<Customer> findCustomersByIdCust(@Param("customer_id") Long customer_id);
}
