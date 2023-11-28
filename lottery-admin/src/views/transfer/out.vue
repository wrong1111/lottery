<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" size="mini">
      <el-form-item label="彩种状态" prop="name">
        <el-select v-model="queryParams.state">
          <el-option v-for="(item,index) in states" :key="item.id" :value="item.value" :label="item.label"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-circle-plus-outline" size="mini" @click="addNewShop"
          plain>设置转单</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="voList" border>
      <el-table-column label="店名" align="center" prop="shopName" />
      <el-table-column label="状态" align="center" prop="states">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.states==1" type="danger">禁用</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="ID" align="center" prop="lotteryType" />
      <el-table-column label="彩种名称" align="center" prop="lotteryName" />
      <el-table-column label="LOGO" align="center">
        <template slot-scope="scope">
          <el-image style="width: 60px; height: 60px" :src="scope.row.icon" fit="cover"></el-image>
        </template>
      </el-table-column>
      <el-table-column label="转单形式" align="center">
        <template slot-scope="scope">
          <span
            :style="{ color: scope.row.transferOutAuto=='0' ? 'red' : '' }">{{ scope.row.transferOutAuto == "0" ? "自动" : "手动" }}</span>
        </template>
      </el-table-column>

      <el-table-column label="截止前(秒)" align="center" prop="transferBeforeTime" />
      <el-table-column label="返点(%)" align="center" prop="commiss" />
      <el-table-column label="创建时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" plain @click="editStateRow(scope.row,0)"
            v-if="scope.row.transferOutAuto==1">自动</el-button>
          <el-button size="mini" type="success" plain @click="editStateRow(scope.row,1)"
            v-if="scope.row.transferOutAuto==0">手动</el-button>

          <el-button size="mini" type="danger" plain @click="editDisables(scope.row,1)"
            v-if="scope.row.states==0">禁用</el-button>
          <el-button size="mini" type="danger" plain @click="editDisables(scope.row,0)"
            v-if="scope.row.states==1">开启</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" />
    <!--
    <AddOutTransfer title="转单设置" :visible.sync="dialogAddVisible" @confirm="addShopReq"  :form="form">
    </AddOutTransfer> -->

  </div>
</template>

<script>
  import {
    getRemoteLotteryInfo,
    getShopall,
    getChangeList,
    editAutoState,
    editDisable
  } from "@/api/transfer/out";
  import {
    deepClone
  } from "@/utils";
  import {
    Message,
    MessageBox
  } from "element-ui";
  // import AddOutTransfer from "./components/AddOutTransfer.vue";

  export default {
    name: "transfeout",
    components: {

    },
    props: {},
    data() {
      return {
        states: [{
          'value': '',
          'label': '所有'
        }, {
          'value': 0,
          'label': '正常'
        }, {
          'value': 1,
          'label': '停止'
        }],
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 彩种列表
        voList: [],
        // 遮罩层
        loading: true,
        // 用户查询
        queryParams: {
          pageNo: 1,
          pageSize: 10,
          state: undefined,
          lotteryId: undefined,
          shopId: undefined
        },
        // 添加用户弹窗
        dialogAddVisible: false,
        // 当前需要修改的用户
        updateShop: {},
      };
    },
    computed: {},
    watch: {},
    created() {},
    mounted() {
      this.getList()
    },
    methods: {
      //
      editDisables(row, state) {
        editDisable({'id':row.id, 'states':state}).then((res) => {
          if (res.success) {
            this.getList()
          }
        })
      },
      //修改
      editTransfer(row) {

      },
      editStateRow(row, state) {
        editAutoState({
          id: row.id,
          states: state
        }).then((res) => {
          this.getList()
        })
      },
      // 获取用户列表
      getList() {
        this.loading = true;
        getChangeList(this.queryParams).then((response) => {
          this.loading = false;
          if (response.success) {
            this.voList = response.voList
            this.total = response.total
          }

        });
      },
      // 条件查询
      handleQuery() {
        this.queryParams.pageNo = 1;
        this.getList();
      },
      // 重置查询条件
      resetQuery() {
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // ---------------- 用户相关操作 -----------------------
      // 新增用户
      addNewShop() {
        //this.dialogAddVisible = true;
        this.$prompt('请输入地址', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
        }).then(({
          value
        }) => {
          getRemoteLotteryInfo({
            url: value
          }).then((res) => {
            if (res.success) {
              this.$message({
                type: 'success',
                message: '一键获取成功'
              });
              this.getList()
            }

          })

        }).catch(() => {
          this.$message({
            type: 'info',
            message: '取消输入'
          });
        });
      },

      // 发送新增 收单彩种请求
      addShopReq(data) {
        addLotteryTransfer(data).then((response) => {
          if (!response.errorCode) {
            this.getList()
          }
        });
      },

    },
  };
</script>

<style scoped lang="scss">
  .shop {
    width: 30%
  }

  .red {
    color: red;
    background-color: #c00000;
  }
</style>
