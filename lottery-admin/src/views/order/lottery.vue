<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" size="mini">
      <el-form-item label="订单号" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="订单状态" clearable>
          <el-option v-for="item in stateOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="赛事类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="赛事类型" clearable>
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否已上传票据" prop="bill">
        <el-select v-model="queryParams.bill" placeholder="是否有票据" clearable>
          <el-option v-for="item in bills" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单类型" prop="transferType">
        <el-select v-model="queryParams.transferType" placeholder="是否有票据" clearable>
          <el-option v-for="item in orderTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" icon="el-icon-receiving" size="mini" plain @click="awardAll">
          一键派奖
        </el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button type="warning" icon="el-icon-takeaway-box" size="mini" plain @click="ticketAll">
          一键出票
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" icon="el-icon-takeaway-box" size="mini" plain @click="syncChangeStateAll">
          一键同步转单状态
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" icon="el-icon-sort" size="mini" plain @click="toggleExpandAll">
          展开/折叠
        </el-button>
      </el-col>

      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="table" v-if="refreshTable" v-loading="loading" :data="voList" border stripe row-key="id"
      :default-expand-all="isExpand">
      <el-table-column type="expand">
        <template slot-scope="scope">
          <el-card>
            <el-form label-position="left" inline class="demo-table-expand">
              <el-row :gutter="5">
                <el-col :span="8">
                  <el-form-item label="订单号：">
                    <span>{{ scope.row.orderId }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="用户名：">
                    <span>{{ scope.row.nickname }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="上级：">
                    <span>{{ scope.row.parentName }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="订单状态：">
                    <span :class="getAward(scope.row)">{{
                      getOrderState(scope.row)
                    }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="下注金额：">
                    <span>{{ scope.row.price }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="预计奖金：">
                    <span>{{ scope.row.forecast }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="中奖金额：">
                    <span :class="getAward(scope.row)">{{ (scope.row.winPrice>0?scope.row.winPrice:'') }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item>
                    <el-tag size="mini" type="success">{{
                      getBettingNotes(scope.row)
                    }}</el-tag>
                    <el-tag class="ml5" size="mini" type="warning">{{
                      getBettingTime(scope.row)
                    }}</el-tag>
                    <template v-if="isSportRace(scope.row)">
                      <el-tag v-for="item in getBettingStyle(scope.row)" :key="item" class="ml5" size="mini"
                        type="danger">{{ item }}</el-tag>
                    </template>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="创建时间：">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="出票时间：">
                    <span>{{ parseTime(scope.row.ticketingTime) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="订单标识：">
                    <span
                      :class="0 == scope.row.transferType?'red1':(1==scope.row.transferType?'blue1':'')">{{ getTransferType(scope.row) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="转单时间：">
                    <span
                      :class="0 == scope.row.transferType?'red1':(1==scope.row.transferType?'blue1':'')">{{ 0 == scope.row.transferType?parseTime(scope.row.createTime):parseTime(scope.row.transferTime)}}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <template v-if="isSportRace(scope.row)">
                    <el-table :data="scope.row.racingBallList" border stripe>
                      <el-table-column label="编号" width="80" align="center">
                        <template slot-scope="inner">
                          {{ inner.row.content.number }}
                        </template>
                      </el-table-column>
                      <el-table-column label="比赛队伍" align="center">
                        <template slot-scope="inner">
                          <span>{{ inner.row.content.homeTeam }}</span>
                          <span :class="getRaceletBallStyle(inner.row)">{{
                            getRaceletBall(inner.row)
                          }}</span>
                          VS
                          <span>{{ inner.row.content.visitingTeam }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column prop="deadline" label="比赛时间" align="center">

                      </el-table-column>
                      <el-table-column label="下注内容" align="center">
                        <template slot-scope="inner">
                          <div v-if="showDan(inner.row)" class="blue">[胆]</div>

                          {{ getRaceContent(scope.row.type,inner.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column label="赛果(全/半)" align="center">
                        <template slot-scope="inner">
                          {{ getRaceResult(inner.row) }}
                        </template>
                      </el-table-column>
                    </el-table>
                  </template>
                  <template v-else>
                    <el-table :data="scope.row.racingBallList" border stripe>
                      <el-table-column label="期号" prop="no" width="80" align="center">
                      </el-table-column>
                      <el-table-column label="投注方式" align="center">
                        <template slot-scope="inner">
                          <span>{{ getNoSportsBettingStyle(scope.row.type,inner.row.type) }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="下注内容" prop="content" align="center">
                        <template slot-scope="inner">
                          <span>{{ inner.row.content }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="赛果" prop="reward" align="center">
                      </el-table-column>
                    </el-table>
                  </template>
                </el-col>
                <el-col :span="24">
                  <el-form-item>
                    <template v-if="scope.row.state === '0'">
                      <el-button size="mini" type="success" @click="ticketSigle(scope.row)">出票</el-button>
                      <!-- <el-button size="mini" type="warning" @click="refuseSigle(scope.row)">拒绝</el-button> -->
                    </template>
                    <el-button size="mini" type="danger" @click="retreatSigle(scope.row)">退票</el-button>
                    <el-button size="mini" type="primary" @click="showInfo(scope.row)">详情</el-button>
                    <el-button size="mini" type="success" @click="upload(scope.row)">上传票据</el-button>
                    <template v-if="scope.row.transferType==null">
                      <el-button size="mini" type="primary" @click="changeSigle(scope.row.id)">转单</el-button>
                    </template>
                    <template v-if="scope.row.transferType==1">
                      <el-button size="mini" type="success" @click="syncChangeState(scope.row.id)">同步转单状态</el-button>
                    </template>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <ImageUpload :multiple="true" :handleChildData="handleSuc" :value=scope.row.bill></ImageUpload>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </template>
      </el-table-column>
      <el-table-column label="昵称" align="center" prop="nickname" width="100" :show-overflow-tooltip="true" />
      <el-table-column label="中奖金额" align="center" width="80">
        <template slot-scope="scope">
          <span :class="getAward(scope.row)">{{ scope.row.winPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下注金额" align="center" prop="price" width="100" :show-overflow-tooltip="true" />
      <el-table-column label="预测金额" align="center" prop="forecast" width="100" :show-overflow-tooltip="true" />
      <el-table-column label="订单状态" align="center" width="80" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span :class="getAward(scope.row)">{{ getOrderState(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="赛事类型" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ getMatchType(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出票时间" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.ticketingTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单类型" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <span
            :class="0 == scope.row.transferType?'red1':(1==scope.row.transferType?'blue1':'')">{{ getTransferType(scope.row) }}</span>
        </template>
      </el-table-column>
     <!-- <el-table-column label="转单时间" align="center" prop="transferTime" show-overflow-tooltip>
      </el-table-column> -->
    </el-table>
    <el-drawer :title="title" :visible.sync="drawer" :with-header="true" :show-close="true"
      :style="{ height: '1400px' }" style="overflow-y: auto;">
      <div>
        彩种 {{lotName}}
        <el-table :data="itemInfo" border v-if="itemInfo.length>0" name="table1" :height="drawerHeight"
          :row-class-name="myclass" :key="digitId">
          <el-table-column prop="idx" width="100" align="center" label="序 号">

          </el-table-column>
          <el-table-column prop="stageNumber" width="100" align="center" label="期 号">

          </el-table-column>
          <el-table-column prop="cont" width="180" align="center" label="投注内容">
            <template slot-scope="scope">
              {{getContent(scope.row.cont)}}
            </template>
          </el-table-column>
          <el-table-column prop="mode" width="180" align="center" label="玩法">
            <template slot-scope="scope">
              {{getNoSportsBettingStyle(lotId,scope.row.mode)}}
            </template>
          </el-table-column>
          <el-table-column prop="reaward" width="100" align="center" label="结果">
            <template slot-scope="scope">
              {{ scope.row.award?  scope.row.money:''}}
            </template>
          </el-table-column>
        </el-table>

        <el-table :data="sportItemInfo" border v-if="sportItemInfo.length>0" name="table2" :height="drawerHeight"
          :key="sportId">
          <el-table-column prop="id" width="80" align="center" label="序 号"> </el-table-column>
          <el-table-column prop="reawardx" width="80" align="center" label="结果">
            <template slot-scope="scope">
              <span :class="scope.row.awardx?'red':''">
                {{ scope.row.awardx? scope.row.moneyx :''}}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="types" width="100" align="center" label="玩法" />
          <el-table-column prop="ballCombinationList" width="380" align="center" label="投注内容">
            <template slot-scope="scope">
              <span v-for="(item,index) in scope.row.ballCombinationList">
                <span prop="number">{{item.number}}</span>
                <span prop="homeTeam" style="margin-left: 10px;">{{item.homeTeam}}</span> VS
                <span prop="visitingTeam" style="margin-left: 10px;">{{item.visitingTeam}}</span>
                <span prop="content" style="margin-left: 10px;">{{item.content}}</span>
                </br>
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" />
  </div>
</template>

<script>
  import {
    getLottryOrderList,
    orderAward,
    orderTicketing,
    orderRetreat,
    orderActual,
    orderChange,
    orderChangeState,
  } from "@/api/order";
  import {
    removeUser
  } from "@/utils/auth";

  import ImageUpload from '@/components/ImageUpload/index.vue';
  export default {
    name: "OrderLottery",
    components: {
      ImageUpload
    },
    props: {},
    data() {
      return {
        sportId: Math.random(),
        digitId: Math.random(),
        bills: [{
          label: "无票据",
          value: "0",
        }, {
          label: "有票据",
          value: "1",
        }],
        orderTypes: [{
          label: "所有",
          value: "",
        },{
          label: "普通",
          value: "2",
        }, {
          label: "收单",
          value: "0",
        }, {
          label: "转单",
          value: "1",
        }],
        fileList: [],
        drawerHeight: 800,
        title: '方案详情',
        //竞赛详情
        sportItemInfo: [],
        //方案内容详情
        itemInfo: [],
        orderNo: '',
        lotName: '',
        lotId: '',
        //方案详情在最右侧展示
        drawer: false,
        // 遮罩层
        loading: false,
        // 总数据条数
        total: 0,
        // 列表数据
        voList: [],
        // 查询参数
        queryParams: {
          orderId: undefined,
          state: undefined,
          type: undefined,
          phone: undefined,
          pageNo: 1,
          pageSize: 10,
          bill: undefined,
          transferType: undefined
        },
        // 是否显示搜索
        showSearch: true,
        // 是否全部展开
        isExpand: true,
        // 重新渲染表格状态
        refreshTable: true,
        // 订单状态
        stateOptions: [{
            label: "待出票",
            value: "0",
          },
          {
            label: "待开奖",
            value: "1",
          },
          {
            label: "未中奖",
            value: "2",
          },
          {
            label: "待派奖",
            value: "3",
          },
          {
            label: "已派奖",
            value: "4",
          },
          {
            label: "已拒绝",
            value: "5",
          },
          {
            label: "已退票",
            value: "6",
          },
        ],
        // 赛事类型
        typeOptions: [{
            label: "竞彩足球",
            value: "0",
          },
          {
            label: "竞彩篮球",
            value: "1",
          },
          {
            label: "北京单场",
            value: "2",
          },
          {
            label: "排列3",
            value: "3",
          },
          {
            label: "排列5",
            value: "4",
          },
          {
            label: "七星彩",
            value: "5",
          },
          {
            label: "14场胜负",
            value: "6",
          },
          {
            label: "任选九",
            value: "7",
          },
          {
            label: "大乐透",
            value: "8",
          },
          {
            label: "福彩3D",
            value: "21",
          },
          {
            label: "七乐彩",
            value: "22",
          },
          {
            label: "快乐8",
            value: "23",
          },
          {
            label: "双色球",
            value: "24",
          }
        ],
      };
    },
    computed: {},
    watch: {},
    created() {},
    mounted() {
      this.getWindowHeight()
      this.getList()
    },
    methods: {
      getTransferType(row) {
      //  console.log(row.transferType, row.transferType == 0)
        if (typeof(row.transferType) == 'undefined' || null == row.transferType) {
          return "普通"
        }
        if (row.transferType === 0) {
          return "收单[" + row.transferShopName + "]-" + row.transferShopId
        }
        return "转单[" + row.transferShopName + "]-" + row.transferOrderNo
      },
      getWindowHeight() {
        this.drawerHeight = window.innerHeight - 80
      },
      //-------上传图片使用的
      handleSuc(data) {
        let that = this
        //        console.log(' upload return filesize ',data)
        that.fileList = data
      },
      upload(row) {
        //console.log(' commit row', row, this.fileList)
        if (this.fileList == null || this.fileList.length == 0) {
          this.$alert('请先上传再处理')
          return
        }

        orderActual({
          id: row.id,
          bill: this.fileList.map(item => item.url).join(',')
        }).then((response) => {
          if (!response.errorCode) {
            this.getList();
          }
        });
      },
      //=======end
      myclass(row) {
        if (row.award) {
          return 'awardRow'
        }
        return ''
      },
      getContent(txt) {
        return txt
      },
      syncChangeStateAll(){
         this.syncChangeState('')
      },
      //单个 同步转单 状态
      syncChangeState(id) {
        orderChangeState({
          'id': id
        }).then((res) => {
          this.$message({
            type: res.success ? 'success' : 'warning',
            message: (res.success && res.data) ? res.data : res.errorMsg
          });
          this.getList()
        })
      },
      //单个转单
      changeSigle(id) {
        orderChange(id).then((res) => {
          if (res.success) {
            //console.log(res)
            this.$message({
              type: 'success',
              message: res.data
            });
            this.getList()
          }
        })
      },
      //展示详细注数号码
      showInfo(row) {
        // console.log(' info ', row)
        let that = this
        that.itemInfo = []
        that.sportItemInfo = []

        that.lotId = row.type
        that.title = row.orderId + '  方案详情'
        that.orderNo = row.orderId
        let lot = that.typeOptions.filter(p => p.value == row.type)
        that.lotName = lot[0].label
        that.drawer = true
        //竞猜展示
        if (that.isSportRace(row) && row.schemeDetails != null) {
          let idx = 1
          let items = row.schemeDetails
          for (let i = 0; i < items.length; i++) {
            const o = {
              id: idx++,
              types: items[i]['type'],
              reawardx: (typeof(items[i]['award']) == 'undefined' || !items[i]['award']) ? false : true,
              awardx: typeof(items[i]['award']) == 'undefined' ? false : items[i]['award'],
              moneyx: typeof(items[i]['money']) == 'undefined' ? '0' : items[i]['money'],
              ballCombinationList: items[i]['ballCombinationList'],
            }
            that.sportItemInfo.push(o)
          }
        //  console.log('sportItemInfo', that.sportItemInfo)
          return
        } else if (!that.isSportRace(row)) {
          //数字展示
          //console.log(row.schemeDetails[0] && row.schemeDetails !=null)
          let items = row.schemeDetails
          let itemArys = []
          for (let i = 0; i < items.length; i++) {
            itemArys.push({
              idx: i + 1,
              cont: items[i]['content'],
              mode: items[i]['mode'],
              stageNumber: items[i]['stageNumber'],
              reaward: typeof(items[i]['award']) == 'undefined' ? false : true,
              award: typeof(items[i]['award']) == 'undefined' ? false : items[i]['award'],
              money: typeof(items[i]['money']) == 'undefined' ? '0' : items[i]['money'],
            })
          }

          that.itemInfo = itemArys
        }
      },
      //show 胆
      showDan(row) {
        if (row.content.hasOwnProperty("isGallbladder") && row.content.isGallbladder) {
          return true
        }
        return false
      },
      // 获取统计列表
      getList() {
        this.loading = true;
        getLottryOrderList(this.queryParams).then((response) => {
          this.loading = false;
          if (!response.errorCode) {
            this.total = response.total;
            const volist = response.voList;
            for (let index = 0; index < volist.length; index++) {
              const outter = volist[index];
              //数字彩的内容。
              if (outter.schemeDetails != null) {
                outter.schemeDetails = JSON.parse(outter.schemeDetails)
              }
              for (let i = 0; i < outter.racingBallList.length; i++) {
                const inner = outter.racingBallList[i];
                const content = inner.content;
                try {
                  const contentObject = JSON.parse(content);
                  inner.content = contentObject;
                } catch (error) {}
              }
            }
            this.voList = volist;
          }
        });
      },
      // 查询数据
      handleQuery() {
        this.queryParams.pageNo = 1;
        this.getList();
      },
      // 重置数据
      resetQuery() {
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 展开/收缩
      toggleExpandAll() {
        this.refreshTable = false;
        this.isExpand = !this.isExpand;
        this.$nextTick(() => {
          this.refreshTable = true;
        });
      },
      // ---------------- 比赛 -----------------------
      // 根据比赛显示数据
      // 0 足彩 1 篮彩 2 北京单场 3 排列3 4排列5  5七星彩  6 14场胜负 7任选九 8大乐透 21 3D，22七乐彩 23 快乐8，24 双色球
      isSportRace(row) {
        // console.log(row.type)
        if (row.type == "3" || row.type == "4" || row.type == "5" || row.type == "8" || row.type == "21" || row
          .type == "22" || row.type == "23" || row.type == "24") {
          return false;
        }
        return true;
      },
      // 比赛让球数
      getRaceletBall(row) {
        // 不存在让球
        if (!row.content.letBall) {
          return "";
        }
        // 让球为0
        if (row.content.letBall === "0") {
          return "";
        }
        return "(" + row.content.letBall + ")";
      },
      // 比赛让球数样式
      getRaceletBallStyle(row) {
        const content = row.content;
        // 不存在让球
        if (!content.letBall) {
          return "";
        }
        // 让球为0
        if (content.letBall === "0") {
          return "";
        }
        let result = content.letBall.includes("+");
        return result ? "red" : "blue";
      },
      // 下注内容
      getRaceContent(type, row) {
        let result = "";
        // console.log(' show content', type, row)
        if (type == 1) {
          let content = row.content
          //胜分
          if (content.winNegativeOddsList && content.winNegativeOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.winNegativeOddsList.length; index++) {
              const element = content.winNegativeOddsList[index];
              const result = element.describe + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
          //让分胜负
          if (content.cedePointsOddsList && content.cedePointsOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.cedePointsOddsList.length; index++) {
              const element = content.cedePointsOddsList[index];
              const result = '让分' + element.describe + "[" + content.cedePoints + "]" + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
          //总分
          if (content.sizeOddsList && content.sizeOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.sizeOddsList.length; index++) {
              const element = content.sizeOddsList[index];
              const result = element.describe + "[" + element.score + "]" + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
          //胜分差
          if (content.differenceOddsList && content.differenceOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.differenceOddsList.length; index++) {
              const element = content.differenceOddsList[index];
              const result = element.describe + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
        } else {
          const content = row.content;
          // 让球胜平负
          if (content.letOddsList && content.letOddsList.length) {
            let oddsList = [];
            for (let index = 0; index < content.letOddsList.length; index++) {
              const element = content.letOddsList[index];
              const result = "让" + element.describe + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
          // 胜平负
          if (content.notLetOddsList && content.notLetOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.notLetOddsList.length; index++) {
              const element = content.notLetOddsList[index];
              const result = element.describe + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
          // 总进球数
          if (content.goalOddsList && content.goalOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.goalOddsList.length; index++) {
              const element = content.goalOddsList[index];
              const result = element.describe + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
          // 半全场
          if (content.halfWholeOddsList && content.halfWholeOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.halfWholeOddsList.length; index++) {
              const element = content.halfWholeOddsList[index];
              const result = element.describe + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
          // 比分
          if (content.scoreOddsList && content.scoreOddsList.length) {
            if (result) {
              result = result + "|";
            }
            let oddsList = [];
            for (let index = 0; index < content.scoreOddsList.length; index++) {
              const element = content.scoreOddsList[index];
              const result = element.describe + "(" + element.odds + ")";
              oddsList.push(result);
            }
            result = result + oddsList.join(",");
          }
        }
        return result;
      },
      // 赛果
      getRaceResult(row) {

        if (!row.reward) {
          return "";
        }
        let result = row.reward.split(",");
        if (result.length < 2) {
          return "";
        }
        let all = result[1];
        let half = result[0];
        let r = all + "\n" + "半" + half;
        //目的就是为了展示让字
        return r + "\n" + row.award.replace(/,/, ',让')
      },
      // ---------------- 其他格式化 -----------------------
      // 投注数量
      getBettingNotes(row) {
        if (this.isSportRace(row)) {
          if (row.racingBallList.length) {
            return row.racingBallList[0].notes + "注"
          }
          return ''
        }
        if (row.racingBallList.length) {
          let counts = 0
          row.racingBallList.map(item => counts += item.notes)
          return counts + "注"
        }
        return "";
      },
      // 投注倍数
      getBettingTime(row) {
        if (row.racingBallList.length) {
          return row.racingBallList[0].times + "倍";
        }
        return "";
      },
      // 投注方式
      getBettingStyle(row) {
        const styleList = [];
        if (row.racingBallList.length && row.racingBallList[0].type) {
          let list = row.racingBallList[0].type.split(",");
          for (let index = 0; index < list.length; index++) {
            const element = list[index];
            styleList.push(element + "串一");
          }
        }
        return styleList;
      },
      // 非体育赛事投注方式
      getNoSportsBettingStyle(lotid, mode) {
        //0 直选 1 组三 2 组九
        if (lotid === "3") { //排列3 mode 3 和值
          switch (mode) {
            case "0":
              return "直选"
            case "1":
              return "组三"
            case "2":
              return "组六"
            case "3":
              return '直选和值'
            case "4":
              return '组选和值'
            case "5":
              return '组三复式'
            default:
              return ''
          }
        } else if (lotid === "23") { // 快乐8 23
          switch (mode) {
            case "10":
              return "选十"
            case "9":
              return "选九"
            case "8":
              return "选八"
            case "7":
              return "选七"
            case "6":
              return "选六"
            case "5":
              return "选五"
            case "4":
              return "选四"
            case "3":
              return "选三"
            case "2":
              return "选二"
            case "1":
              return "选一"

            default:
              return ''
          }
        } else {
          switch (mode) {
            case "0":
              return "直选";
            case "1":
              return "组三";
            case "2":
              return "组六";
            default:
              return "直选";
          }
        }

      },
      // 中奖Style
      getAward(row) {
        if (row.state == "4" || row.state == '3') {
          return "award";
        }
        return "";
      },
      // 获取订单状态
      getOrderState(row) {
        let result = this.stateOptions.filter((item) => {
          return item.value === row.state;
        });
        if (result.length === 0) {
          return "";
        }
        return result[0].label;
      },
      // 获取赛事类型
      getMatchType(row) {
        let result = this.typeOptions.filter((item) => {
          return item.value === row.type;
        });
        if (result.length === 0) {
          return "";
        }
        return result[0].label;
      },
      // 一键派奖
      awardAll() {
        //弹窗提示，是否有足够的店铺保证金扣除
        this.$confirm('确定要 一键派奖 吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 用户点击了确认按钮，执行后续处理
          // 在这里编写你需要执行的代码
          const data = {
            //id: row.id, 没有ID默认未出的全出，
          };
          orderAward(data).then((response) => {
            if (!response.errorCode) {
              this.getList();
            }
          });
        }).catch(() => {
          // 用户点击了取消按钮，可以选择执行一些其他操作
        });
      },
      // 单个派奖
      awardSigle(row) {

      },
      // 一键出票
      ticketAll() {
        //弹窗提示，是否有足够的店铺保证金扣除
        this.$confirm('确定要 一键出票 吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 用户点击了确认按钮，执行后续处理
          // 在这里编写你需要执行的代码
          const data = {
            state: "1",
            //id: row.id, 没有ID默认未出的全出，
          };
          orderTicketing(data).then((response) => {
            if (!response.errorCode) {
              this.getList();
            }
          });
        }).catch(() => {
          // 用户点击了取消按钮，可以选择执行一些其他操作
        });
      },
      // 单个出票
      ticketSigle(row) {
        //  console.log(row);
        const data = {
          state: "1",
          id: row.id,
        };
        orderTicketing(data).then((response) => {
          if (!response.errorCode) {
            this.getList();
          }
        });
      },
      // 单个拒绝
      refuseSigle(row) {
        const data = {
          state: "5",
          id: row.id,
        };
        orderTicketing(data).then((response) => {
          if (!response.errorCode) {
            this.getList();
          }
        });
      },
      // 单个退票
      retreatSigle(row) {
        orderRetreat(row.id).then((response) => {
          if (!response.errorCode) {
            this.getList();
          }
        });
      },
    },
  };
</script>

<style scoped lang="scss">
  ::v-deep .el-table__expanded-cell[class*="cell"] {
    padding: 10px;
  }

  // 滚动条的宽度
  ::v-deep .el-table__body-wrapper::-webkit-scrollbar {
    width: 6px; // 横向滚动条
    height: 6px; // 纵向滚动条 必写
  }

  // 滚动条的滑块
  ::v-deep .el-table__body-wrapper::-webkit-scrollbar-thumb {
    background-color: #ddd;
    border-radius: 3px;
  }

  .award {
    color: red;

  }

  .awardRow {
    background-color: red;
  }

  .red1 {
    color: red;
    font-size: 18px;
  }

  .blue1 {
    color: blue;
    font-size: 18px;
  }

  .red {
    color: red;
  }

  .blue {
    color: blue;
  }

  .el-drawer__body {
    overflow: auto;
  }

  .demo-table-expand {
    font-size: 0;

    ::v-deep .el-form-item__label {
      padding: 0px;
    }

    .label {
      width: 90px;
      color: #99a9bf;
    }

    .el-form-item {
      margin-right: 0;
      margin-bottom: 0;
      width: 50%;
    }
  }
</style>
