import Vue from "vue";
import Router from "vue-router";

Vue.use(Router);

/* Layout */
import Layout from "@/layout";

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [{
    path: "/login",
    component: () => import("@/views/login/index"),
    hidden: true,
  },

  {
    path: "/404",
    component: () => import("@/views/404"),
    hidden: true,
  },

  {
    path: "/",
    component: Layout,
    redirect: "/dashboard",
    children: [{
      path: "dashboard",
      name: "Dashboard",
      component: () => import("@/views/dashboard/index"),
      meta: {
        title: "首页",
        icon: "el-icon-data-board",
        affix: true
      },
    }, ],
  },
  {
    path: "/client",
    component: Layout,
    redirect: "/client/manager",
    meta: {
      title: "用户管理",
      icon: "el-icon-user"
    },
    children: [{
        path: "manager",
        name: "ClientManager",
        component: () => import("@/views/client/manager"),
        meta: {
          title: "用户管理"
        },
      },
      {
        path: "statistics",
        name: "ClientStatistics",
        component: () => import("@/views/client/statistics"),
        meta: {
          title: "客户统计"
        },
      },
      {
        path: "system",
        name: "ClientSystem",
        component: () => import("@/views/client/system"),
        meta: {
          title: "系统用户"
        },
      },
    ],
  },

  {
    path: "/ballgame",
    component: Layout,
    redirect: "/ballgame/set",
    meta: {
      title: "网站管理",
      icon: "el-icon-menu"
    },
    children: [{
        path: "set",
        name: "BallgameSet",
        component: () => import("@/views/ballgame/set.vue"),
        meta: {
          title: "栏目设置"
        },
      },
      {
        path: "notice",
        name: "BallgameNotice",
        component: () => import("@/views/ballgame/notice.vue"),
        meta: {
          title: "公告设置"
        },
      },
      {
        path: "domain",
        name: "BallgameDomain",
        component: () => import("@/views/ballgame/domain.vue"),
        meta: {
          title: "域名设置"
        },
      },
    ],
  },

  {
    path: "/order",
    component: Layout,
    redirect: "/order/pay",
    meta: {
      title: "订单管理",
      icon: "el-icon-s-operation"
    },
    children: [{
        path: "pay",
        name: "OrderPay",
        component: () => import("@/views/order/pay.vue"),
        meta: {
          title: "流水订单"
        },
      },
      {
        path: "lottery",
        name: "OrderLottery",
        component: () => import("@/views/order/lottery.vue"),
        meta: {
          title: "订单列表"
        },
      },
    ],
  },

  {
    path: "/log",
    component: Layout,
    redirect: "/log/list",
    alwaysShow: true,
    meta: {
      title: "日志管理",
      icon: "el-icon-notebook-2",
      affix: true
    },
    children: [{
      path: "list",
      name: "Log",
      component: () => import("@/views/log/index.vue"),
      meta: {
        title: "操作日志"
      },
    }, ],
  },

  {
    path: "/withdrawal",
    component: Layout,
    redirect: "/withdrawal/list",
    alwaysShow: true,
    meta: {
      title: "提现管理",
      icon: "el-icon-wallet",
      affix: true
    },
    children: [{
      path: "list",
      name: "WithdrawalList",
      component: () => import("@/views/withdrawal/index.vue"),
      meta: {
        title: "提现列表"
      },
    }, ],
  },

  {
    path: "/shop",
    component: Layout,
    redirect: "/shop/list",
    alwaysShow: true,
    meta: {
      title: "店铺管理",
      icon: "el-icon-s-shop",
      affix: true
    },
    children: [{
      path: "list",
      name: "ShopList",
      component: () => import("@/views/shop/index.vue"),
      meta: {
        title: "店铺管理"
      },
    }, ],
  },
  {
    path: "/transferin",
    component: Layout,
    redirect: "/transferin/list",
    alwaysShow: true,
    meta: {
      title: "收单管理",
      icon: "el-icon-s-operation",
      affix: true
    },
    children: [{
      path: "list",
      name: "transfein",
      component: () => import("@/views/transfer/in.vue"),
      meta: {
        title: "收单彩种设置"
      },
    }, {
      path: "platin",
      name: "platin",
      component: () => import("@/views/transfer/inplat.vue"),
      meta: {
        title: "下游商家管理"
      },
    },],
  },
  {
    path: "/transferout",
    component: Layout,
    redirect: "/transferout/list",
    alwaysShow: true,
    meta: {
      title: "转单管理",
      icon: "el-icon-s-operation",
      affix: true
    },
    children: [{
        path: "list",
        name: "transfeout",
        component: () => import("@/views/transfer/out.vue"),
        meta: {
          title: "转单设置"
        },
      },

    ],
  },
  // 404 page must be placed at the end !!!
  {
    path: "*",
    redirect: "/404",
    hidden: true
  },
];

const createRouter = () =>
  new Router({
    // mode: 'history', // require service support
    scrollBehavior: () => ({
      y: 0
    }),
    routes: constantRoutes,
  });

const router = createRouter();

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter();
  router.matcher = newRouter.matcher; // reset router
}

export default router;
