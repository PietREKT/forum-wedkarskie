import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
    const auth = useAuthStore()

    // wymaga logowania
    if (to.meta.requiresAuth && !auth.isAuthenticated) {
        return next({
            path: '/login',
            query: { redirect: to.fullPath },
        })
    }

    // wymaga admina
    if (to.meta.requiresAdmin && !auth.isAdmin) {
        return next({ path: '/profile' })
    }

    next()
})

export default router
