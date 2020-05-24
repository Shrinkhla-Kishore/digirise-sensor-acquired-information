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
public interface GatewayRepository extends CrudRepository<Gateway, Long> {
    @Query("select gw from Gateway gw where gw.name = :name and gw.customer = :customer")
    public Stream<Gateway> findGatewayByName(@Param("name") String name, @Param("customer") Customer customer);
}
