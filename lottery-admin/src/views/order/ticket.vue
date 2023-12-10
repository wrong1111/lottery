<template>
  <div class="admin-card">
    <el-card :class="item.ticketState==2?'box-card revoke':'box-card'" v-for="(item,index) in ticketCard">
      <template slot-scope="scope">
        <div class="clearfix">
          <span>第<span class="red" style="font-size:2em;">{{item.ticketNo}}</span>票 注数：{{item.bets}} 倍数: <span class="blue"
              style="font-size:1em;">{{item.times}}</span> 金额:
            <span class="red" style="font-size: 18px;font-weight: bold;">{{item.price}}</span>
            <span style="margin-left: 40px;">共{{ticketCard.length}}票</span>
          </span>
        </div>
        <div class="item text">
          <div>
            <el-row style="border: 1px solid cornflowerblue;" v-for="(it,idx) in item.ticketContent">
              <el-col :span="12">
                <span>{{it.homeTeam}}</span>
                <span :class="it.letBall.indexOf('-')!=-1?'red':'blue'">{{it.letBall}} </span>
                <span>{{it.visitingTeam}}</span>
              </el-col>
              <el-col :span="12"><span v-for="(it2,idx2) in it.ticketContentVOList">
                  <span
                    v-if="item.type ==1 && it2.mode==3">{{it2.describe+"["+it2.letball+"]"+"("+ it2.odds+")"}}</span>
                  <span v-else>{{it2.describe+"("+ it2.odds+")"}}</span>
                </span></el-col>
            </el-row>
            <el-row
              style="border: 1px solid cadetblue;display: flex;justify-content: center;justify-items: center;flex-direction: row;">
              <el-col :span="12"
                style="display: flex;flex-direction: row;justify-content: center;justify-items: center;">
                <span>预测奖金</span> <span class="red">{{item.forecast}}</span>
              </el-col>
              <el-col :span="12">
                <span
                  :class="(item.state==3||item.state==4)?'red':(item.ticketState ==2||item.state==5)?'stray':'blue'">{{showState(item)}}</span>
              </el-col>
            </el-row>
            <el-row
              style="border: 1px solid cadetblue;display: flex;justify-content: center;justify-items: center;flex-direction: row;">
              <el-col :span="12">
                <el-button type="warning" icon="el-icon-takeaway-box" size="mini" plain
                  v-if="!(item.ticketState ==2||item.state==5)" @click="revoke(item)">退票</el-button>
              </el-col>
              <el-col :span="12">
                <el-button type="success" icon="el-icon-takeaway-box" size="mini" plain
                  v-if="!(item.ticketState ==2||item.state==5) && item.times>1" @click="edit(item)">调整倍数</el-button>
              </el-col>

            </el-row>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script>
  import {
    editTicketMulti,
    retreatTicket,
    listTicket,
  } from "@/api/order";
  import {
    MessageBox
  } from "element-ui";
  export default {
    name: "Ticket",
    components: {

    },
    //props: ['ticketCard'],
    mounted() {
      //  console.log('mounted', this.ticketCard)
      this.orderId = this.$route.query.id
      // this.orderId = this.$route.params.order.orderId
      this.getList()
    },
    data() {
      return {
        orderId: '',
        ticketCard: []
      }
    },
    methods: {
      getList() {
        listTicket(this.orderId).then((res) => {
          if (res.success) {
            let arys = res.voList
            arys.map((item) => {
              item.ticketContent = JSON.parse(item.ticketContent)
            })
            this.ticketCard = arys
          }
        })
      },
      showState(item) {
        if (item.ticketState == 2) {
          return '已退票'
        }
        if (item.state == 0) {
          return '待出票'
        } else if (item.state == 1) {
          return '待开奖'
        } else if (item.state == 2) {
          return '未中奖'
        } else if (item.state == 3) {
          return '待派奖(' + item.winPrice + ')'
        } else if (item.state == 4) {
          return '已中奖(' + item.winPrice + ')'
        } else if (item.state == 5 || item.state == 6) {
          return '已退票'
        }
      },
      revoke(data) {
        //console.log('revoke', data)
        this.$confirm('此操作将做退票处理, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          retreatTicket(data.id).then((res) => {
            if (res.success) {
              this.$message({
                type: 'success',
                message: '退票成功!'
              });
              this.getList()
            } else {
              //  MessageBox.alert('','',{'title':'错误提示','message':res.errorMsg,'type':'error'})
              this.$message({
                type: 'error',
                message: res.errorMsg
              });
            }
          })
        }).catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消'
          // });
        });
      },
      edit(data) {
        console.log('edit', data)
        this.$prompt('请输入调整的倍数必须小于或等于票面倍数', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPattern: /\d+/,
            inputErrorMessage: '数据格式不正确'
          }).then(({
              value
            }) => {
              // this.$message({
              //   type: 'success',
              //   message: '你的邮箱是: ' + value
              // });
              editTicketMulti(data.id, value).then((res) => {
                  if (res.success) {
                    this.$message({
                      type: 'success',
                      message: '调整倍数成功!'
                    });
                    this.getList()
                  } else {
                    this.$message({
                      type: 'error',
                      message: res.errorMsg
                    });
                    }
                  })
              }).catch(() => {
              // this.$message({
              //   type: 'info',
              //   message: '取消输入'
              // });
            });
          },
          handleClick(r, e) {
            console.log(r, e)
          },
      }
    }
</script>

<style scoped lang="scss">
  .stray {
    color: silver;
    text-underline-offset: 0.0625rem;
  }

  .red {

    color: red
  }

  .blue {
    color: blue
  }

  .box-card {
    width: 560px;
    position: relative;
  }

  .revoke {
    background-color: lightgray;
  }

  .el-col {
    display: flex;
    justify-content: center;
    justify-items: center;
    flex-wrap: wrap;
    -webkit-box-pack: center;
    -ms-flex-pack: center;
    -ms-flex-wrap: wrap;
    flex-wrap: wrap;
    flex-direction: column;

    span {
      line-height: 25px;
      font-size: 15px;
      margin: 5px 0 5px 5px;
    }
  }

  .item {
    margin-bottom: 18px;
  }

  .text {
    font-size: 14px;
  }



  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }

  .clearfix:after {
    clear: both
  }

  .admin-card {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-around;
    margin-top: 10px;
  }

  .stamp {
    position: absolute;
    top: -10px;
    right: -10px;
    width: 30px;
    height: 30px;
    background-color: red;
    color: white;
    text-align: center;
    line-height: 30px;
    font-weight: bold;
    border-radius: 50%;
  }

  .stamp::after {
    content: "";
    position: absolute;
    top: 0;
    right: 0;
    width: 10px;
    height: 10px;
    background-color: red;
    border-radius: 50%;
  }
</style>
