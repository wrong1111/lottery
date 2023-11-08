<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="voList" border stripe>
      <el-table-column label="id" align="center" prop="id" width="100" fixed />
      <el-table-column
        label="公告"
        align="center"
        prop="msg"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="创建时间" align="center" width="200">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
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
            @click="editNotice(scope.row)"
            >修改</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      title="修改公告"
      :visible.sync="dialogVisible"
      width="500px"
      append-to-body
    >
      <el-form ref="noticeForm" :model="form" label-width="80px" :rules="rules">
        <el-form-item label="公告内容" prop="msg">
          <el-input v-model="form.msg" type="textarea" placeholder="请输入内容" />
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
import { getNoticeList, updateNotice } from "@/api/ballgame";

export default {
  name: "BallgameNotice",
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
        msg: "",
      },
      // 表单校验
      rules: {
        msg: [{ required: true, message: "公告不能为空", trigger: "blur" }],
      },
      // 选中修改公告ID
      noticeId: "",
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
      getNoticeList().then((response) => {
        this.loading = false;

        if (!response.errorCode) {
          this.voList = response.voList;
        }
      });
    },
    // 修改公告
    editNotice(row) {
      this.noticeId = row.id;
      this.form.msg = row.msg;
      this.dialogVisible = true;
    },
    // 提交修改内容
    submitForm() {
      this.$refs["noticeForm"].validate((valid) => {
        if (!valid) {
          return;
        }

        this.dialogVisible = false;
        if (!this.noticeId) {
          return;
        }
        let params = {
          msg: this.form.msg,
          id: this.noticeId,
        };
        updateNotice(this.noticeId, params).then((response) => {
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
