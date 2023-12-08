package com.qihang.service.order;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qihang.domain.order.LotteryTicketDO;
import com.qihang.mapper.order.LotteryTicketMapper;
import org.springframework.stereotype.Service;


@Service
public class LotteryTicketServiceImpl extends ServiceImpl<LotteryTicketMapper, LotteryTicketDO> implements ILotteryTicketService {
}
