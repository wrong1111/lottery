<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="voList" border stripe>
      <el-table-column label="id" align="center" prop="id" width="100" fixed />
      <el-table-column
        label="APP前端地址"
        align="center"
        prop="appUrl"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="APP下载地址"
        align="center"
        prop="downloadUrl"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="修改时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="updateDomainAction(scope.row)"
            >修改</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      title="修改域名"
      :visible.sync="dialogVisible"
      width="680px"
      append-to-body
    >
      <el-form ref="dominForm" :model="form" label-width="120px" :rules="rules">
        <el-form-item label="APP前端地址" prop="appUrl">
          <el-input v-model="form.appUrl" placeholder="请输入前端地址" />
        </el-form-item>
        <el-form-item label="APP下载地址" prop="downloadUrl">
          <el-input v-model="form.downloadUrl" placeholder="请输入下载地址" />
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDomainList, updateDomain } from "@/api/ballgame";

export default {
  name: "BallgameDomain",
  components: {},
  props: {},
  data() {
    return {
      // 遮罩层
      loading: false,
      // 数据
      voList: [],
      // 弹框是否可见
      dialogVisible: false,
      // 表单参数
      form: {
        id: undefined,
        appUrl: undefined,
        downloadUrl: undefined,
        updateTime: undefined,
      },
      // 表单校验
      rules: {
        appUrl: [{ required: true, message: "地址不能为空", trigger: "blur" }],
        downloadUrl: [{ required: true, message: "地址不能为空", trigger: "blur" }],
      },
    };
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {
    this.getList();
  },
  methods: {
    // 获取统计列表
    getList() {
      this.loading = true;
      getDomainList().then((response) => {
        this.loading = false;

        if (!response.errorCode) {
          this.voList = response.voList;
        }
      });
    },
    // 修改域名
    updateDomainAction(row) {
      this.form.id = row.id;
      this.form.appUrl = row.appUrl;
      this.form.downloadUrl = row.downloadUrl;

      this.dialogVisible = true;
    },
    // 提交修改内容
    submitForm() {
      this.$refs["dominForm"].validate((valid) => {
        if (!valid) {
          return;
        }

        this.dialogVisible = false;
        updateDomain(this.form.id, this.form).then((response) => {
          if (!response.errorCode) {
            this.getList();
          }
        });
      });
    },
  },
};
</script>

<style scoped lang="scss"></style>
