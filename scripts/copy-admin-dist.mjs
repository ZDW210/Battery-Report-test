import { cp, mkdir, rm } from 'node:fs/promises'
import { resolve } from 'node:path'

const root = resolve(import.meta.dirname, '..')
const source = resolve(root, 'admin-vue3/dist-worker')
const target = resolve(root, 'worker/public')

await rm(target, { recursive: true, force: true })
await mkdir(target, { recursive: true })
await cp(source, target, { recursive: true })
