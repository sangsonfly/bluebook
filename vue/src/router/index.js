import { createRouter, createWebHistory } from 'vue-router'
import { projectName } from '../../config/config.default'

const routes = [
  //通用路由
  {
    path: '/',
    name: '/',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      title: '注册'
    }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue'),
    meta: {
      title: '404'
    }
  },
  //下面都是前台路由
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/Front.vue'),
    children: [
      // 前台子路由
      {
        path: 'home',
        name: 'FrontHome',
        component: () => import('../views/front/Home.vue'),
        meta: {
          title: '前台首页',
          keepAlive: true
        }
      },
      {
        path: 'home/note/:id',
        redirect: to => `/front/note/${to.params.id}`
      },
      {
        path: 'password',
        name: 'FrontPassword',
        component: () => import('../views/front/Password.vue'),
        meta: {
          title: '修改密码'
        }
      },
      {
        path: 'person',
        name: 'FrontPerson',
        component: () => import('../views/front/Person.vue'),
        meta: {
          title: '个人信息'
        }
      },
      {
        path: 'note/:id',
        name: 'NoteDetail',
        component: () => import('../views/front/NoteDetail.vue'),
        meta: {
          title: '笔记详情'
        }
      },
      {
        path: 'publish',
        name: 'PublishNote',
        component: () => import('../views/front/Publish.vue'),
        meta: {
          title: '发布笔记'
        }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('../views/front/UserProfile.vue'),
        meta: {
          title: '用户主页'
        }
      },
      {
        path: 'chat/:targetUserId?',
        name: 'PrivateChat',
        component: () => import('../views/front/Chat.vue'),
        meta: {
          title: '私聊'
        }
      },
      {
        path: 'club/:id/manage',
        name: 'ClubManage',
        component: () => import('../views/front/ClubManage.vue'),
        meta: {
          title: '社团管理'
        }
      },
      {
        path: 'club/:id',
        name: 'ClubDetail',
        component: () => import('../views/front/ClubDetail.vue'),
        meta: {
          title: '社团主页'
        }
      },
      // 前台子路由
    ]
  },
  //下面都是后台路由
  {
    path: '/back',
    name: 'back',
    component: () => import('../views/Back.vue'),
    children: [
      // 后台子路由
      {
        path: 'home',
        name: 'BackHome',
        component: () => import('../views/back/Home.vue'),
        meta: {
          title: '后台首页'
        }
      },
      {
        path: 'password',
        name: 'BackPassword',
        component: () => import('../views/back/Password.vue'),
        meta: {
          title: '修改密码'
        }
      },
      {
        path: 'adminPerson',
        name: 'BackAdminPerson',
        component: () => import('../views/back/AdminPerson.vue'),
        meta: {
          title: '个人信息'
        }
      },
      {
        path: 'user',
        name: 'BackUser',
        component: () => import('../views/back/User.vue'),
        meta: {
          title: '用户管理'
        }
      },
      {
        path: 'admin',
        name: 'BackAdmin',
        component: () => import('../views/back/Admin.vue'),
        meta: {
          title: '管理员管理'
        }
      },
      {
        path: 'note',
        name: 'BackNote',
        component: () => import('../views/back/Note.vue'),
        meta: {
          title: '笔记管理'
        }
      },
      {
        path: 'club',
        name: 'BackClub',
        component: () => import('../views/back/Club.vue'),
        meta: {
          title: '社团管理'
        }
      },
      {
        path: 'comment',
        name: 'BackComment',
        component: () => import('../views/back/Comment.vue'),
        meta: {
          title: '评论管理'
        }
      },
      {
        path: 'statistics',
        name: 'BackStatistics',
        component: () => import('../views/back/Statistics.vue'),
        meta: {
          title: '数据统计'
        }
      },
      // 后台子路由
    ]
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const account = JSON.parse(localStorage.getItem("account") || '{}')
  if (to.matched.length === 0) {
    next('/404')
    return
  }
  if (to.path === '/') {
    if (account.role) {
      // 现在是只有角色为管理员才访问后台
      // 如果想设置其他角色登录后也默认访问后台，可以用下面的判断条件
      // account.role === 'ROLE_ADMIN' || account.role === 'ROLE_UNIT'
      if (account.role === 'ROLE_ADMIN') {
        next('/back/home')
      } else {
        next('/front/home')
      }
    } else {
      // 现在是只有登录以后才可以访问首页
      next('/login')
      // 如果想不登录就可以直接访问首页的话，直接用下面的跳转/front/home即可
      // next('/front/home')
    }
  } else {
    next()
  }
})

// 全局后置守卫
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - ${projectName}` : projectName // 设置页面标题
})

export default router
