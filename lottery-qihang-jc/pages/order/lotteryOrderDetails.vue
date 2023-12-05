<template>
	<!-- 购彩订单详情列表 -->
	<view class="box">
		<cmd-nav-bar @iconOne="backClick" iconOne="chevron-left" title="订单详情" font-color="#fff"
			background-color="#FF3F43">
		</cmd-nav-bar>

		<view class="info">
			<view style="display: flex;justify-content: space-around;align-items: center;padding-top: 40px">
				<view>
					<p clsaa="data">{{lotteryOrder.winPrice|formatWinPrice}}</p>
					<p class="font">中奖金额</p>
				</view>
				<view>
					<p clsaa="data">{{lotteryOrder.state|formatState}}</p>
					<p class="font">订单状态</p>
				</view>
				<view>
					<p clsaa="data">{{lotteryOrder.price}}元</p>
					<p class="font">投注金额</p>
				</view>
			</view>
			<view style="padding-top: 15px;"
				v-if="lotteryOrder.type=='0'||lotteryOrder.type=='1'||lotteryOrder.type=='2'">
				<u-button size="normal" shape="circle" customStyle="color:#000;width:92%;height:35px" color="#fff">
					预测奖金：<span style="color:#FF3F43">￥{{lotteryOrder.forecast}}</span>
				</u-button>
			</view>
		</view>
		<view class="body">
			<!-- 福彩3D 21 排列3 WYONG EDIT-->
			<uni-card is-shadow v-if="lotteryOrder.type=='3' ||lotteryOrder.type =='21'  ">
				<view>
					<span class="title">{{lotteryOrder.ballName}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
						<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
					</p>
				</view>
				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="50" align="center">期号</uni-th>
						<uni-th width="50" align="center">投注内容</uni-th>
						<uni-th width="50" align="center">投注方式</uni-th>
						<uni-th width="50" align="center">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.recordList" :key="index">
						<uni-td width="50" align="center">{{record.stageNumber}}</uni-td>
						<uni-td width="50" align="center">
							{{record.hundred}} <span v-if="record.hundred!=null">|</span> {{record.ten}}
							<span v-if="record.ten!=null">|</span>
							<span v-if="record.mode=='1'||record.mode=='2'" v-for="(c,index) in record.individual">
								{{c.num}}
								<span style="color: #FF3F43;" v-if="c.isGallbladder">[胆]</span>
								<span v-if="index<record.individual.length-1">,</span>
							</span>
							<span v-if="record.mode!='1'&&record.mode!='2'">
								{{record.individual}}
							</span>
						</uni-td>
						<uni-td width="50" align="center">{{record.mode|formatMode}}</uni-td>
						<uni-td width="50" align="center">{{record.reward}}</uni-td>
					</uni-tr>
				</uni-table>

			</uni-card>
			<!-- 排列5 4 -->
			<uni-card is-shadow v-if="lotteryOrder.type=='4'">
				<view>
					<span class="title">{{lotteryOrder.ballName}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
						<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
					</p>
				</view>
				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="15" align="center">期号</uni-th>
						<uni-th width="65" align="center">投注内容</uni-th>
						<uni-th width="65" align="center">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.recordList" :key="index">
						<uni-td align="center">{{record.stageNumber}}</uni-td>
						<uni-td align="center">{{record.myriad}} | {{record.kilo}} | {{record.hundred}} |
							{{record.ten}} |
							{{record.individual}}
						</uni-td>
						<uni-td align="center">{{record.reward}}</uni-td>
					</uni-tr>
				</uni-table>
			</uni-card>
			<!-- 七星彩 5 -->
			<uni-card is-shadow v-if="lotteryOrder.type=='5'">
				<view>
					<span class="title">{{lotteryOrder.ballName}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
						<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
					</p>
				</view>
				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="15" align="center">期号</uni-th>
						<uni-th width="65" align="center">投注内容</uni-th>
						<uni-th width="65" align="center">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.recordList" :key="index">
						<uni-td align="center">{{record.stageNumber}}</uni-td>
						<uni-td align="center">{{record.hundredMyriad}} |{{record.tenMyriad}} |{{record.myriad}} |
							{{record.kilo}} | {{record.hundred}} |
							{{record.ten}} |
							{{record.individual}}
						</uni-td>
						<uni-td align="center">{{record.reward}}</uni-td>
					</uni-tr>
				</uni-table>
			</uni-card>
			<!-- 双色球24 大乐透 8 -->
			<uni-card is-shadow v-if="lotteryOrder.type=='8' ||lotteryOrder.type == 24">
				<view>
					<span class="title">{{lotteryOrder.ballName}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
						<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
					</p>
				</view>
				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="15" align="center">期号</uni-th>
						<uni-th width="65" align="center">投注内容</uni-th>
						<uni-th width="65" align="center">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.recordList" :key="index">
						<uni-td align="center">{{record.stageNumber}}</uni-td>
						<uni-td align="center">
							<span v-for="(c,index) in record.ten">
								{{c.num}}
								<span style="color: #FF3F43;" v-if="c.isGallbladder">[胆]</span>
								<span v-if="index<record.ten.length-1">,</span>
							</span>
							<span style="padding: 0px 5px">|</span>
							<span v-for="(c,index) in record.individual">
								{{c.num}}
								<span style="color: #FF3F43;" v-if="c.isGallbladder">[胆]</span>
								<span v-if="index<record.individual.length-1">,</span>
							</span>
						</uni-td>
						<uni-td align="center">{{record.reward}}</uni-td>
					</uni-tr>
				</uni-table>
			</uni-card>
			<!-- 七乐彩 22-->
			<uni-card is-shadow v-if="lotteryOrder.type == 22">
				<view>
					<span class="title">{{lotteryOrder.ballName}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
						<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
					</p>
				</view>
				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="15" align="center">期号</uni-th>
						<uni-th width="65" align="center">投注内容</uni-th>
						<uni-th width="65" align="center">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.recordList" :key="index">
						<uni-td align="center">{{record.stageNumber}}</uni-td>
						<uni-td align="center">
							<span v-for="(c,index) in record.individual">
								{{c.num}}
								<span style="color: #FF3F43;" v-if="c.isGallbladder">[胆]</span>
								<span v-if="index<record.individual.length-1">,</span>
							</span>
						</uni-td>
						<uni-td align="center">{{record.reward}}</uni-td>
					</uni-tr>
				</uni-table>
			</uni-card>
			<!-- 快乐8 -->
			<uni-card is-shadow v-if="lotteryOrder.type == 23">
				<view>
					<span class="title">{{lotteryOrder.ballName}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
						<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
					</p>
				</view>
				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="15" align="center">期号</uni-th>
						<uni-th width="65" align="center">投注内容</uni-th>
						<uni-th width="65" align="center">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.recordList" :key="index">
						<uni-td align="center">{{record.stageNumber}}</uni-td>
						<uni-td align="center">
							<span v-for="(c,index) in record.individual">
								{{c.num}}
								<span style="color: #FF3F43;" v-if="c.isGallbladder">[胆]</span>
								<span v-if="index<record.individual.length-1">,</span>
							</span>
						</uni-td>
						<uni-td align="center">{{record.reward}}</uni-td>
					</uni-tr>
				</uni-table>
			</uni-card>

			<!-- 竞彩足球 0-->
			<uni-card is-shadow v-if="lotteryOrder.type=='0'">
				<view v-if="lotteryOrder.documentaryFlag&&!lotteryOrder.openFlag&&!lotteryOrder.isEnd"
					style="display: flex;flex-direction: column;justify-content: center;align-items: center;color: grey;font-size: 20px;">
					<u-icon name="lock" size="50" color="grey"></u-icon>
					<text style="margin-top: 10px;">开赛后可见</text>
					<text style="margin: 20px 0px;">截止时间 {{lotteryOrder.deadline|formatDate(that)}}</text>
				</view>
				<view v-else>
					<view>
						<span class="title">{{lotteryOrder.ballName}}</span>
						<p style="display: flex;justify-content: flex-end;align-items: center;">
							<uni-row>
								<uni-col :span="lotteryOrder.pssTypeList.length<=2?12:24">
									<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
									<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
								</uni-col>
								<uni-col :span="6" :key="index" v-for="(item,index) in lotteryOrder.pssTypeList">
									<u-tag v-if="item==1" :borderColor="index|mapTagColor" :bgColor="index|mapTagColor"
										text="单关"
										:style="lotteryOrder.pssTypeList.length==1?'margin-left: 35px;':'margin-left: 10px;'">
									</u-tag>
									<u-tag v-else :borderColor="index|mapTagColor" :bgColor="index|mapTagColor"
										:text="item+'串一'"
										:style="lotteryOrder.pssTypeList.length==1?'margin-left: 35px;':'margin-left: 10px;'">
									</u-tag>
								</uni-col>
							</uni-row>
						</p>
					</view>
					<uni-table stripe emptyText="暂无更多数据">
						<!-- 表头行 -->
						<uni-tr>
							<uni-th width="10" align="center">场次</uni-th>
							<uni-th width="100" align="center">主队/客队</uni-th>
							<uni-th width="65" align="center">投注内容</uni-th>
							<uni-th width="50" align="center">赛果<br>(全/半)</uni-th>
						</uni-tr>
						<!-- 表格数据行 -->
						<uni-tr v-for="(record,index) in lotteryOrder.ballInfoList" :key="index">
							<uni-td align="center">{{record.number}}</uni-td>
							<uni-td align="center">{{record.homeTeam}}
								<span class="rangqiu" :class="{rangqiuBlue:record.letBall < 0}"><br>
									({{record.letBall}})</span>
								<br>{{record.visitingTeam}}
							</uni-td>
							<uni-td align="center">
								<!-- notLet.describe==record.award[0] 如果选择的和中奖的结果一样标记为红色 -->
								<span :style="notLet.describe==record.award[0]?'color:#FF3F43':''"
									v-for="(notLet,index) in record.content.notLetOddsList" v-if="notLet.active">
									{{notLet.describe}}({{notLet.odds}})<br>
								</span>

								<span :style="record.halfFullCourt!=undefined&&lets.describe==(Number(record.halfFullCourt.split(',')[1].split(':')[0])+Number(record.letBall)>Number(record.halfFullCourt.split(',')[1].split(':')[1])?'胜'
								:Number(record.halfFullCourt.split(',')[1].split(':')[0])+Number(record.letBall)<Number(record.halfFullCourt.split(',')[1].split(':')[1])?'负'
								:'平')?'color:#FF3F43':''" v-for="(lets,index) in record.content.letOddsList" v-if="lets.active">
									让{{lets.describe}}({{lets.odds}})<br>
								</span>
								<span :style="goal.describe==record.award[2]?'color:#FF3F43':''"
									v-for="(goal,index) in record.content.goalOddsList" v-if="goal.active">
									{{goal.describe}}({{goal.odds}})<br>
								</span>
								<span :style="half.describe==record.award[3]?'color:#FF3F43':''"
									v-for="(half,index) in record.content.halfWholeOddsList" v-if="half.active">
									{{half.describe}}({{half.odds}})<br>
								</span>
								<span :style="score.describe==record.award[4]?'color:#FF3F43':''"
									v-for="(score,index) in record.content.scoreOddsList" v-if="score.active">
									{{score.describe}}({{score.odds}})<br>
								</span>
							</uni-td>
							<uni-td align="center">
								<span v-if="record.halfFullCourt!=undefined">
									{{record.halfFullCourt.split(',')[1]}}<br>半{{record.halfFullCourt.split(',')[0]}}
								</span>
							</uni-td>
						</uni-tr>
					</uni-table>
				</view>
			</uni-card>
			<!-- 竞彩篮球 1-->
			<uni-card is-shadow v-if="lotteryOrder.type=='1'">
				<view v-if="lotteryOrder.documentaryFlag&&!lotteryOrder.openFlag&&!lotteryOrder.isEnd"
					style="display: flex;flex-direction: column;justify-content: center;align-items: center;color: grey;font-size: 20px;">
					<u-icon name="lock" size="50" color="grey"></u-icon>
					<text style="margin-top: 10px;">开赛后可见</text>
					<text style="margin: 20px 0px;">截止时间 {{lotteryOrder.deadline|formatDate(that)}}</text>
				</view>
				<view v-else>
					<view>
						<span class="title">{{lotteryOrder.ballName}}</span>
						<p style="display: flex;justify-content: flex-end;align-items: center;">
							<uni-row>
								<uni-col :span="lotteryOrder.pssTypeList.length<=2?12:24">
									<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
									<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
								</uni-col>
								<uni-col :span="6" :key="index" v-for="(item,index) in lotteryOrder.pssTypeList">
									<u-tag v-if="item==1" :borderColor="index|mapTagColor" :bgColor="index|mapTagColor"
										text="单关"
										:style="lotteryOrder.pssTypeList.length==1?'margin-left: 35px;':'margin-left: 10px;'">
									</u-tag>
									<u-tag v-else :borderColor="index|mapTagColor" :bgColor="index|mapTagColor"
										:text="item+'串一'"
										:style="lotteryOrder.pssTypeList.length==1?'margin-left: 35px;':'margin-left: 10px;'">
									</u-tag>
								</uni-col>
							</uni-row>
						</p>
					</view>
					<uni-table stripe emptyText="暂无更多数据">
						<!-- 表头行 -->
						<uni-tr>
							<uni-th width="10" align="center">场次</uni-th>
							<uni-th width="100" align="center">客队/主队</uni-th>
							<uni-th width="75" align="center">投注内容</uni-th>
							<uni-th width="40" align="center">赛果</uni-th>
						</uni-tr>
						<!-- 表格数据行 -->
						<uni-tr v-for="(record,index) in lotteryOrder.ballInfoList" :key="index">
							<uni-td align="center">{{record.number}}</uni-td>
							<uni-td align="center">{{record.visitingTeam}}
								<span class="rangqiu" :class="{rangqiuBlue:record.letBall < 0}"><br>
									({{record.letBall}})</span>
								<br>{{record.homeTeam}}
							</uni-td>
							<uni-td align="center">
								<span :style="winNegative.describe==record.award[0]?'color:#FF3F43':''"
									v-for="(winNegative,index) in record.content.winNegativeOddsList"
									v-if="winNegative.active">
									{{winNegative.describe}}({{winNegative.odds}})<br>
								</span>
								<span :style="record.halfFullCourt!=undefined&&cedePoints.describe==(Number(record.halfFullCourt.split(':')[1])+Number(record.letBall)>Number(record.halfFullCourt.split(':')[0])?'主胜'
								:'主负')?'color:#FF3F43':''" v-for="(cedePoints,index) in record.content.cedePointsOddsList"
									v-if="cedePoints.active">
									让{{cedePoints.describe}}({{cedePoints.odds}})<br>
								</span>
								<span :style="record.halfFullCourt!=undefined&&size.describe==(Number(record.halfFullCourt.split(':')[1])+Number(record.halfFullCourt.split(':')[0])>Number(size.score)?'大'
								:'小')?'color:#FF3F43':''" v-for="(size,index) in record.content.sizeOddsList" v-if="size.active">
									{{size.describe}}<span
										style="color: #1afa29;">[{{size.score}}]</span>({{size.odds}})<br>
								</span>
								<span :style="difference.describe==record.award[2]?'color:#FF3F43':''"
									v-for="(difference,index) in record.content.differenceOddsList"
									v-if="difference.active">
									{{difference.describe}}({{difference.odds}})<br>
								</span>
							</uni-td>
							<uni-td align="center">
								<span v-if="record.halfFullCourt!=undefined">
									{{record.halfFullCourt}}
								</span>
							</uni-td>
						</uni-tr>
					</uni-table>
				</view>
			</uni-card>
			<!-- 北京单场 2-->
			<uni-card is-shadow v-if="lotteryOrder.type=='2' || lotteryOrder.type ==25">
				<view>
					<span class="title">{{lotteryOrder.ballName}} {{lotteryOrder.type ==25?'-胜负过关':''}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<uni-row>
							<uni-col :span="lotteryOrder.pssTypeList.length<=2?12:24">
								<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
								<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
							</uni-col>
							<uni-col :span="6" :key="index" v-for="(item,index) in lotteryOrder.pssTypeList">
								<u-tag v-if="item==1" :borderColor="index|mapTagColor" :bgColor="index|mapTagColor"
									text="单关"
									:style="lotteryOrder.pssTypeList.length==1?'margin-left: 35px;':'margin-left: 10px;'">
								</u-tag>
								<u-tag v-else :borderColor="index|mapTagColor" :bgColor="index|mapTagColor"
									:text="item+'串一'"
									:style="lotteryOrder.pssTypeList.length==1?'margin-left: 35px;':'margin-left: 10px;'">
								</u-tag>
							</uni-col>
						</uni-row>
					</p>
				</view>

				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="10" align="center">场次</uni-th>
						<uni-th width="100" align="center">主队/客队</uni-th>
						<uni-th width="65" align="center">投注内容</uni-th>
						<uni-th width="50" align="center" v-if="lotteryOrder.type!=25">赛果<br>(全/半)</uni-th>
						<uni-th width="50" align="center" v-if="lotteryOrder.type==25">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.ballInfoList" :key="index">
						<uni-td align="center">{{record.number}}</uni-td>
						<uni-td align="center">{{record.homeTeam}}
							<span class="rangqiu" :class="{rangqiuBlue:record.letBall < 0}"><br>
								({{record.letBall}})</span>
							<br>{{record.visitingTeam}}
						</uni-td>
						<uni-td align="center">
							<span :style="record.halfFullCourt!=undefined&&lets.describe==(Number(record.halfFullCourt.split(',')[1].split(':')[0])+Number(record.letBall)>Number(record.halfFullCourt.split(',')[1].split(':')[1])?'胜'
								:Number(record.halfFullCourt.split(',')[1].split(':')[0])+Number(record.letBall)<Number(record.halfFullCourt.split(',')[1].split(':')[1])?'负'
								:'平')?'color:#FF3F43':''" v-for="(lets,index) in record.content.letOddsList" v-if="lets.active">
								<!-- 判断如果中了就显示开奖后的赔率，没中的就显示开始下注的赔率 -->
								<span v-if="record.halfFullCourt!=undefined&&lets.describe==(Number(record.halfFullCourt.split(',')[1].split(':')[0])+Number(record.letBall)>Number(record.halfFullCourt.split(',')[1].split(':')[1])?'胜'
								:Number(record.halfFullCourt.split(',')[1].split(':')[0])+Number(record.letBall)<Number(record.halfFullCourt.split(',')[1].split(':')[1])?'负'
								:'平')">让{{lets.describe}}({{record.bonusOdds[0]}})</span>
								<span v-else>让{{lets.describe}}({{lets.odds}})</span><br>
							</span>
							<span :style="goal.describe==record.award[1]?'color:#FF3F43':''"
								v-for="(goal,index) in record.content.goalOddsList" v-if="goal.active">
								<!-- 判断如果中了就显示开奖后的赔率，没中的就显示开始下注的赔率 -->
								<span v-if="goal.describe==record.award[1]">
									{{goal.describe}}({{record.bonusOdds[1]}})
								</span>
								<span v-else>
									{{goal.describe}}({{goal.odds}})
								</span>
								<br>
							</span>
							<span :style="score.describe==record.award[2]?'color:#FF3F43':''"
								v-for="(score,index) in record.content.scoreOddsList" v-if="score.active">
								<!-- 判断如果中了就显示开奖后的赔率，没中的就显示开始下注的赔率 -->
								<span v-if="score.describe==record.award[2]">
									{{score.describe}}({{record.bonusOdds[2]}})
								</span>
								<span v-else>
									{{score.describe}}({{score.odds}})
								</span>
								<br>
							</span>
							<span :style="oddEven.describe==record.award[3]?'color:#FF3F43':''"
								v-for="(oddEven,index) in record.content.oddEvenOdds" v-if="oddEven.active">
								<!-- 判断如果中了就显示开奖后的赔率，没中的就显示开始下注的赔率 -->
								<span v-if="oddEven.describe==record.award[3]">
									{{oddEven.describe}}({{record.bonusOdds[3]}})
								</span>
								<span v-else>
									{{oddEven.describe}}({{oddEven.odds}})
								</span>
								<br>
							</span>
							<span :style="half.describe==record.award[4]?'color:#FF3F43':''"
								v-for="(half,index) in record.content.halfWholeOddsList" v-if="half.active">
								<!-- 判断如果中了就显示开奖后的赔率，没中的就显示开始下注的赔率 -->
								<span v-if="half.describe==record.award[4]">
									{{half.describe}}({{record.bonusOdds[4]}})
								</span>
								<span v-else>
									{{half.describe}}({{half.odds}})
								</span>
								<br>
							</span>
							<span :style="half.describe==record.award[4]?'color:#FF3F43':''"
								v-for="(half,index) in record.content.halfWholeOddsList" v-if="half.active">
								<!-- 判断如果中了就显示开奖后的赔率，没中的就显示开始下注的赔率 -->
								<span v-if="half.describe==record.award[4]">
									{{half.describe}}({{record.bonusOdds[4]}})
								</span>
								<span v-else>
									{{half.describe}}({{half.odds}})
								</span>
								<br>
							</span>
							<span :style="sfgg.describe==record.award[5]?'color:#FF3F43':''"
								v-for="(sfgg,index) in record.content.sfggOdds" v-if="sfgg.active">
								<!-- 判断如果中了就显示开奖后的赔率，没中的就显示开始下注的赔率 -->
								<span v-if="sfgg.describe==record.award[5]">
									{{sfgg.describe}}({{record.bonusOdds[5]}})
								</span>
								<span v-else>
									{{sfgg.describe}}({{sfgg.odds}})
								</span>
								<br>
							</span>
						</uni-td>
						<uni-td align="center">
							<span v-if="record.halfFullCourt!=undefined && lotteryOrder.type !=25" >
								{{record.halfFullCourt.split(',')[1]}}<br>半{{record.halfFullCourt.split(',')[0]}}
							</span>
							<span v-if="record.halfFullCourt!=undefined && lotteryOrder.type ==25" >
								{{record.halfFullCourt}}
							</span>
						</uni-td>
					</uni-tr>
				</uni-table>
			</uni-card>

			<!-- 足球14场 6 任九 7-->
			<uni-card is-shadow v-if="lotteryOrder.type=='6'||lotteryOrder.type=='7'">
				<view>
					<span class="title">{{lotteryOrder.ballName}}</span>
					<p style="display: flex;justify-content: flex-end;align-items: center;">
						<uni-row>
							<uni-col :span="lotteryOrder.pssTypeList.length<=2?12:24">
								<u-tag :text="lotteryOrder.notes+'注'" type="error"></u-tag>
								<u-tag :text="lotteryOrder.times +'倍'" style="margin-left: 10px;"></u-tag>
							</uni-col>
							<uni-col :span="6" :key="index" v-for="(item,index) in lotteryOrder.pssTypeList">
								<u-tag v-if="item==1" :borderColor="index|mapTagColor" :bgColor="index|mapTagColor"
									text="单关"
									:style="lotteryOrder.pssTypeList.length==1?'margin-left: 35px;':'margin-left: 10px;'">
								</u-tag>
							</uni-col>
						</uni-row>
					</p>
				</view>
				<uni-table stripe emptyText="暂无更多数据">
					<!-- 表头行 -->
					<uni-tr>
						<uni-th width="10" align="center">场次</uni-th>
						<uni-th width="100" align="center">主队/客队</uni-th>
						<uni-th width="65" align="center">投注内容</uni-th>
						<uni-th width="50" align="center">赛果</uni-th>
					</uni-tr>
					<!-- 表格数据行 -->
					<uni-tr v-for="(record,index) in lotteryOrder.ballInfoList" :key="index">
						<uni-td align="center">
							<div class="paiban">
								<div>
									<view class="demo-layout bg-purple">{{record.number}}</view>
								</div>
								<div class="content" v-if="record.content.isGallbladder">
									<p>胆</p>
								</div>
							</div>
						</uni-td>
						<uni-td align="center">{{record.homeTeam}}
							<br> VS <br>{{record.visitingTeam}}
						</uni-td>
						<uni-td align="center">
							<!-- notLet.describe==record.award[0] 如果选择的和中奖的结果一样标记为红色 -->
							<span :style="notLet.describe==record.award[0]?'color:#FF3F43':''"
								v-for="(notLet,index) in record.content.notLetOddsList" v-if="notLet.active">
								{{notLet.describe}}<br>
							</span>
						</uni-td>
						<uni-td align="center">
							<span v-if="record.award!=undefined">
								{{record.award[0]}}
							</span>
						</uni-td>
					</uni-tr>
				</uni-table>
			</uni-card>

			<!-- 竞赛方案详情，展示 schemeDetails-->
			<uni-card class="phone"
				v-if="lotteryOrder.schemeDetails!=null&&lotteryOrder.schemeDetails!=undefined&&lotteryOrder.schemeDetails!=''&&isSportType(lotteryOrder.type)">
				<p>
					<view class="title">方案详情</view>
					<uni-table border stripe emptyText="暂无更多数据" class="make">
						<!-- 表头行 -->
						<uni-tr>
							<uni-th width="40px" align="center">过关</uni-th>
							<uni-th align="center" width="100px">单注组合</uni-th>
							<uni-th align="center" width="30px">倍数</uni-th>
							<uni-th align="center" width="80px">预测奖金</uni-th>
						</uni-tr>
						<!-- 表格数据行 -->
						<tbody v-for="(item,index) in lotteryOrder.schemeDetails" :key="index">
							<uni-tr>
								<uni-td align="center">{{item.type}}</uni-td>
								<uni-td align="center">
									<view @click="open(index,item)"
										style="display: flex;justify-content: center;align-items: center;flex-direction: column;font-size: 12px;">
										<span v-for="(ball,idx) in item.ballCombinationList" :key="idx">
											{{ball.homeTeam}}|{{ball.content}}<br>
										</span>
										<u-icon name="arrow-down"></u-icon>
									</view>
								</uni-td>
								<uni-td align="center">
									{{item.notes * (lotteryOrder.betType ==0? lotteryOrder.times:1)}}
								</uni-td>
								<uni-td align="center"
									v-if="lotteryOrder.type!=6 && lotteryOrder.type!=7">{{item.forecastBonus}}</uni-td>
								<uni-td align="center" v-else>
									浮动奖金<br>
									以官网<br>
									出奖为准
								</uni-td>
							</uni-tr>

							<uni-tr v-if="item.isShow" style="background: #FAF9DE">
								<uni-th width="10px" align="center">场次</uni-th>
								<uni-th align="center" width="85px">主队</uni-th>
								<uni-th align="center" width="85px">客队</uni-th>
								<uni-th align="center" width="80px">投注内容</uni-th>
							</uni-tr>
							<uni-tr v-for="(data,idx) in item.ballCombinationList" :key="idx" v-if="item.isShow"
								style="background: #FAF9DE">
								<uni-td align="center">{{data.number}}</uni-td>
								<uni-td align="center">{{data.homeTeam}} <span :style="data.letball.indexOf('-')==-1?'color:red;font-size: 16px;margin-left:5px':'color:blue;font-size: 16px;margin-left:5px'" v-if="data.letball!=null && lotteryOrder.type ==25"></br>({{data.letball}})</span></uni-td>
								<uni-td align="center">{{data.visitingTeam}}</uni-td>
								<uni-td align="center">{{data.content}}</uni-td>
							</uni-tr>
						</tbody>
					</uni-table>
				</p>
			</uni-card>
			<!-- 数字彩 详情 展示 schemeDeatils -->
			<uni-card class="phone"
				v-if="lotteryOrder.schemeDetails!=null&&lotteryOrder.schemeDetails!=undefined&&lotteryOrder.schemeDetails!=''&&!isSportType(lotteryOrder.type)">
				<p>
					<view class=" title">方案详情</view>
					<uni-table border stripe emptyText="暂无更多数据" class="make">
						<!-- 表头行 -->
						<uni-tr>
							<uni-th width="20px" align="center">期号</uni-th>
							<uni-th align="center" width="30px"
								v-if="lotteryOrder.type==3 ||lotteryOrder.type ==21 ||lotteryOrder.type==23">玩法</uni-th>
							<uni-th align="center" width="80px">中奖组合</uni-th>
							<uni-th align="center" width="30px">倍数</uni-th>
							<uni-th align="center" width="120px">预测奖金</uni-th>
						</uni-tr>
						<!-- 表格数据行 -->
						<tbody v-for="(item,index) in lotteryOrder.schemeDetails" :key="index">
							<uni-tr>
								<uni-td align="center">{{item.stageNumber}}</uni-td>
								<uni-td align="center" v-if="lotteryOrder.type==3||lotteryOrder.type==21">
									{{item.mode|formatMode}}
								</uni-td>
								<!-- 快乐8-->
								<uni-td align="center" v-if="lotteryOrder.type==23">
									{{item.mode|formatMode23}}
								</uni-td>
								<uni-td align="center">
									{{item.content}}
								</uni-td>
								<uni-td align="center">
									{{lotteryOrder.times}}
								</uni-td>
								<uni-td align="center"
									v-if="((lotteryOrder.type==3||lotteryOrder.type==4 ||lotteryOrder.type==21||lotteryOrder.type ==23)&&typeof(item.award)=='undefined')">{{item.forecastBonus}}</uni-td>
								<uni-td align="center" v-else-if="typeof(item.award)!='undefined' && item.award">
									<b style="color: #FF3F43;font-size: 18px;">{{item.money}}</b></uni-td>
								<uni-td align="center"
									v-else-if="typeof(item.award)!='undefined' && !item.award">未中奖</uni-td>
								<uni-td align="center" v-else>
									浮动奖金以官网出奖为准
								</uni-td>
							</uni-tr>
						</tbody>
					</uni-table>
				</p>
			</uni-card>

			<uni-card is-shadow class="tip phone">
				<view class="uni-body">订单编号：{{lotteryOrder.orderId}}</view>
				<view class="uni-body">创建时间：{{lotteryOrder.createTime|formatDate(that)}}</view>
				<view class="uni-body">出票时间：{{lotteryOrder.ticketingTime|formatDate(that)}}</view>
				<view class="uni-body" v-if="lotteryOrder.type=='2'">提示：赔率以最后出奖的赔率为准</view>
				<view class="uni-body"
					v-else-if="lotteryOrder.type=='0'|| lotteryOrder.type=='1' ||lotteryOrder.type=='6' || lotteryOrder.type=='7' ">
					提示：赔率以实际出票的赔率为准</view>
				<view class="uni-body" v-else>提示：中奖金额以实际出奖金额的为准</view>
			</uni-card>
			<!-- 彩票出票照片-->
			<uni-card class="phone" v-if="lotteryOrder.bill!=undefined ">
				<p style="">
					<view class="title">彩票照片</view>
				</p>
				<p class="paiban2">
					<u-image @click="imgListPreview(item)" v-for="(item,index) in lotteryOrder.bill" class="img"
						:src="item" :width="100" :height="100"></u-image>
				</p>
			</uni-card>
		</view>

	</view>
</template>

<script>
	import {
		getLotteryOrderPage,
		getLotteryOrderById,
		isSport
	} from '@/api/lotteryOrder.js'
	export default {
		data() {
			return {
				lotteryOrder: {},
				that: this,
			}
		},
		onLoad(option) {
			this.init(option.id);
		},
		filters: {
			mapTagColor(index) {
				switch (index % 20) {
					case 0:
						return '#D52BB3'
					case 1:
						return '#3CC48D'
					case 2:
						return '#d81e06'
					case 3:
						return '#1296db'
					case 4:
						return '#13227a'
					case 5:
						return '#3cc2a8'
					case 6:
						return '#1cedf2'
					case 7:
						return '#6b1cf2'
					case 8:
						return '#e71cf2'
					case 9:
						return '#f28a1c'
					case 10:
						return '#f4ea2a'
					default:
						return '#19be6b'
				}
			},
			formatMode(mode) {
				if (mode == "0") {
					return "直选";
				} else if (mode == "1") {
					return "组三";
				} else if (mode == "2") {
					return "组六";
				} else if (mode == "3") {
					return "直选和值";
				} else if (mode == "4") {
					return "组选和值";
				} else if (mode == "5") {
					return "组三复式";
				}
			},
			formatMode23(mode) {
				if (mode == "10") {
					return "选十";
				} else if (mode == "9") {
					return "选九";
				} else if (mode == "8") {
					return "选八";
				} else if (mode == "7") {
					return "选七";
				} else if (mode == "6") {
					return "选六";
				} else if (mode == "5") {
					return "选五";
				} else if (mode == "4") {
					return "选四";
				} else if (mode == "3") {
					return "选三";
				} else if (mode == "2") {
					return "选二";
				} else if (mode == "1") {
					return "选一";
				}
			},
			//格式化日期
			formatDate(data, that) {
				if (data != null && data != undefined) {
					return that.globalUtil.dateTimeFormat(data)
				}
			},
			formatWinPrice(data) {
				if (data == null || data == undefined || data == 0) {
					return "暂无";
				}
				return data;
			},
			//格式化状态
			formatState(data) {
				if (data == 0) {
					return "待出票";
				} else if (data == 1) {
					return "待开奖";
				} else if (data == 2) {
					return "未中奖";
				} else if (data == 3) {
					return "待派奖";
				} else if (data == 4) {
					return "已派奖";
				} else if (data == 5) {
					return "拒绝";
				} else if (data == 6) {
					return "已退票";
				}
			}
		},
		methods: {
			open(index, item) {
				this.$set(this.lotteryOrder.schemeDetails[index], "isShow", !item.isShow)
			},
			imgListPreview(url) {
				var urlList = []
				urlList.push(url) //push中的参数为 :src="item.img_url" 中的图片地址
				uni.previewImage({
					indicator: "number",
					loop: true,
					urls: urlList
				})
			},
			//返回事件处理
			backClick() {
				//如果是从下单页面过来的直接跳转首页。
				let isPay = uni.getStorageSync('isPay');
				if (isPay) {
					uni.reLaunch({
						url: "/pages/home/home"
					})
				} else {
					uni.navigateBack();
				}
			},
			isSportType(type) {
				if (type == "3" || type == "21" || type == 4 || type == 5 ||
					type == 8 || type == 24 || type == 22 || type == 23) {
					return false
				}
				return true
			},
			//初始化事件
			init(id) {
				uni.showLoading();
				getLotteryOrderById(id).then(res => {
					this.lotteryOrder = res;
					if (typeof(this.lotteryOrder.bill) != 'undefined' && this.lotteryOrder.bill != null && this
						.lotteryOrder.bill.length > 0) {
						this.lotteryOrder.bill = this.lotteryOrder.bill.split(',')
					}
					if (typeof(res.schemeDetails) != 'undefined' && res.schemeDetails != null && res
						.schemeDetails != "") {
						this.lotteryOrder.schemeDetails = JSON.parse(res.schemeDetails)
					}
					//将字符串转对象
					if (this.lotteryOrder.ballInfoList != null) {
						this.lotteryOrder.ballInfoList.map((item, idx) => {
							this.$set(this.lotteryOrder.ballInfoList[idx], "content", JSON.parse(item
								.content))
							//將比賽结果转换成数组，并返回
							if(this.lotteryOrder.type ==25){
								//胜负过关
								if (item.award != null) {
									var moren = ["", "", "", "", "",item.award]
									this.$set(this.lotteryOrder.ballInfoList[idx], "award", moren)
								} else {
									//考虑比赛结果还没有出的话设置一个默认值，防止报错
									var moren = ["", "", "", "", "",""]
									this.$set(this.lotteryOrder.ballInfoList[idx], "award", moren)
								}
								if (item.bonusOdds != null) {
									var moren = ["", "", "", "", "",item.bonusOdds]
									this.$set(this.lotteryOrder.ballInfoList[idx], "bonusOdds", moren)
								} else {
									//考虑比赛结果还没有出的话设置一个默认值，防止报错
									var moren = ["", "", "", "", "",""]
									this.$set(this.lotteryOrder.ballInfoList[idx], "bonusOdds", moren)
								}
							}else{
								
								if (item.award != null) {
									var more = [...item.award.split(','),""]
									this.$set(this.lotteryOrder.ballInfoList[idx], "award", more)
								} else {
									//考虑比赛结果还没有出的话设置一个默认值，防止报错
									var moren = ["", "", "", "", "",""]
									this.$set(this.lotteryOrder.ballInfoList[idx], "award", moren)
								}
								if (item.bonusOdds != null) {
									var   moren = [...item.bonusOdds.split(','),""]
									this.$set(this.lotteryOrder.ballInfoList[idx], "bonusOdds", moren)
								} else {
									//考虑比赛结果还没有出的话设置一个默认值，防止报错
									var moren = ["", "", "", "", "","",""]
									this.$set(this.lotteryOrder.ballInfoList[idx], "bonusOdds", moren)
								}
							}
							
						})
					}
					//处理超过一定高度采用滚动条
					this.$nextTick(() => {
						if (document.querySelector(".make").clientHeight > 500) {
							document.querySelector(".make").setAttribute("class", "scheme")
						}
					})
					//处理排列3的组三和组六的字符串转数组
					//wyong edit 
					if (this.lotteryOrder.type == "3" || this.lotteryOrder.type == "21") {
						this.lotteryOrder.recordList.map(item => {
							if (item.mode == "1" || item.mode == "2") {
								item.individual = JSON.parse(item.individual)
							}
							item.reward = this.lotteryOrder.reaward
						})
					}
					//处理大乐透 ,双色球,七乐彩
					if (this.lotteryOrder.type == "8" || this.lotteryOrder.type == "24" || this.lotteryOrder
						.type == "22" || this.lotteryOrder.type == "23") {
						this.lotteryOrder.recordList.map(item => {
							item.ten = JSON.parse(item.ten)
							item.individual = JSON.parse(item.individual)
							item.reward = this.lotteryOrder.reaward
						})
					}
				})
				setTimeout(function() {
					uni.hideLoading();
				}, 500);
			}
		}
	}
</script>

<style scoped lang="scss">
	.paiban2 {
		display: flex;
		align-items: center;
		justify-content: space-evenly;
		flex-direction: row;
		flex-wrap: wrap;
		width: 100%;
	}

	.paiban {
		display: flex;
		align-items: center;
		justify-content: space-between;
		flex-direction: row;
		width: 100%;
	}

	.content {
		width: 20px;
		height: 20px;
		background-color: #FF3F43;
		border-radius: 50%;

		p {
			width: 20px;
			height: 20px;
			color: #fff;
			text-align: center;
			line-height: 20px;
			font-size: 12px;
		}
	}

	.rangqiuBlue {
		color: #2d8cf0 !important;
	}

	.scheme {
		margin-top: 30px;
		overflow: auto;
		height: 1000rpx;
		overflow-x: hidden;
	}

	/deep/.uni-col {
		padding: 0px 0px 5px 0px !important;
		display: flex !important;
		justify-content: center !important;
		align-items: center !important;
	}

	.rangqiu {
		color: #FF3F43;
	}

	.box {
		padding-bottom: 0px;
		background: #f7f9fa;

		.uni-body {
			color: #909399;
		}

		.info {
			height: 320rpx;
			background: radial-gradient(circle, rgba(63, 94, 251, 1) 0%, rgba(255, 63, 67, 1) 100%);
			color: #ffffff;
			display: flex;
			flex-direction: column;

			view {
				text-align: center;

				.font {
					font-size: 28rpx;
				}

				p {
					padding-bottom: 14rpx;
					font-size: 38rpx;
				}
			}
		}

		.body {
			.uni-card {
				margin: 0px !important;
				padding: 0px !important;
			}

			.title {
				font-size: 28rpx;
			}

			.img {}

			.tip {
				margin-top: 20rpx;

				view {
					margin-top: 20rpx;
				}
			}

			.phone {
				margin-top: 20rpx !important;
				height: 100%;
			}
		}
	}
</style>