package com.madongfang.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, String> {

	public Device findByCode(String deviceCode);
	
	public List<Device> findByLongitudeBetweenAndLatitudeBetween(double minlng, double maxlng, double minlat, double maxlat);
}
