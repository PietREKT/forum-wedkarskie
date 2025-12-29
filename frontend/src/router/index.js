import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
    const auth = useAuthStore()

    if (to.meta.requiresAuth && !auth.isAuthenticated) {
        return next({
            path: '/login',
            query: { redirect: to.fullPath },
        })
    }

    if (to.meta.requiresAdmin && !auth.isAdmin) {
        return next({ path: '/profile' })
    }

    next()
})

export default router
