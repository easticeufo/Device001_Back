package com.madongfang.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.madongfang.api.BillRecordApi;
import com.madongfang.api.CardApi;
import com.madongfang.api.CardRechargeApi;
import com.madongfang.service.CustomRecordService;
import com.madongfang.service.CustomService;

@RestController
@RequestMapping(value="/api/cards")
public class CardController {

	@GetMapping(value="/{cardId}")
	public CardApi getCard(@PathVariable String cardId) {
		return customService.getCard(cardId);
	}
	
	@PostMapping
	public CardApi addCard(@RequestBody CardApi cardApi) {
		return customService.addCard(cardApi);
	}
	
	@PostMapping(value="/{cardId}/recharge")
	public CardApi updateCard(@PathVariable String cardId, @RequestBody CardRechargeApi cardRechargeApi)
	{
		return customService.rechargeCard(cardId, cardRechargeApi);
	}
	
	@GetMapping(value="/{cardId}/records/bill")
	public List<BillRecordApi> getPaymentRecords(@PathVariable String cardId, 
			@RequestParam(required=false, name="beginTime") Long beginTimeMs, 
			@RequestParam(required=false, name="endTime") Long endTimeMs)
	{
		Date beginTime = new Date(0);
		Date endTime = new Date();
		if (beginTimeMs != null)
		{
			beginTime.setTime(beginTimeMs);
		}
		if (endTimeMs != null)
		{
			endTime.setTime(endTimeMs);
		}
		
		return customRecordService.getBillRecords(cardId, beginTime, endTime);
	}
	
	@Autowired
	private CustomService customService;
	
	@Autowired
	private CustomRecordService customRecordService;
}
