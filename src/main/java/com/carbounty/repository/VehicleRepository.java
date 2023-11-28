package com.carbounty.repository;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.carbounty.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

	Page<Vehicle> findAllByDeleted(boolean i, Pageable paging);

	Vehicle findById(int vehicleId);

	Set<Vehicle> searchByModelNameInOrBrandNameInOrFuelTypeInOrTypeInOrTransmissionInAllIgnoreCase(String[] tokens,
			String[] tokens1, String[] tokens2, String[] tokens3, String[] tokens4);

	Set<Vehicle> findBySearchStringIn(Set<String> tokenSet);

	Page<Vehicle> findAllByDeletedAndType(boolean i, String type, Pageable paging);
}
