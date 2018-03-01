package com.madongfang.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.madongfang.api.BillRecordApi;
import com.madongfang.api.ChargeRecordApi;
import com.madongfang.api.CurrentChargeApi;
import com.madongfang.api.CustomApi;
import com.madongfang.api.PaymentRecordApi;
import com.madongfang.entity.Custom;
import com.madongfang.service.CustomRecordService;
import com.madongfang.service.CustomService;
import com.madongfang.service.PlugService;

@RestController
@RequestMapping(value="/api/mine")
public class MineController {

	@GetMapping(value="/info")
	public CustomApi getInfo(@SessionAttribute Custom custom)
	{
		return customService.getCustom(custom.getId());
	}
	
	@GetMapping(value="/currentCharges")
	public List<CurrentChargeApi> getCurrentCharges(@SessionAttribute Custom custom)
	{
		return plugService.getCurrentCharges(custom.getId());
	}
	
	@GetMapping(value="/records/charge",params="last=true")
	public ChargeRecordApi getLastChargeRecord(@SessionAttribute Custom custom) 
	{
		return customRecordService.getLastChargeRecord(custom.getId());
	}
	
	@GetMapping(value="/records/charge", params="page")
	public List<ChargeRecordApi> getChargeRecords(@SessionAttribute Custom custom, 
			@PageableDefault(size=30, sort={"time"}, direction=Direction.DESC) Pageable pageable) 
	{
		return customRecordService.getChargeRecords(custom.getId(), pageable);
	}
	
	@GetMapping(value="/records/payment", params="page")
	public List<PaymentRecordApi> getPaymentRecords(@SessionAttribute Custom custom, 
			@PageableDefault(size=30, sort={"time"}, direction=Direction.DESC) Pageable pageable) 
	{
		return customRecordService.getPaymentRecords(custom.getId(), pageable);
	}
	
	@GetMapping(value="/records/bill", params="page")
	public List<BillRecordApi> getBillRecords(@SessionAttribute Custom custom, 
			@PageableDefault(size=30, sort={"time"}, direction=Direction.DESC) Pageable pageable)
	{
		return customRecordService.getBillRecords(custom.getId(), pageable);
	}
	
	@Autowired
	private CustomService customService;
	
	@Autowired
	private CustomRecordService customRecordService;
	
	@Autowired
	private PlugService plugService;
}
