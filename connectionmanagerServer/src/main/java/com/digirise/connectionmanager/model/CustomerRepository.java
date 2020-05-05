package com.digirise.connectionmanager.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by IntelliJ IDEA.
 * Date: 2020-05-04
 * Author: shrinkhlak
 */

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
