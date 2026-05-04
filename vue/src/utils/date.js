/**
 * 日期格式化工具函数
 * 支持多种日期格式：数组、ISO字符串、普通字符串、对象等
 */

/**
 * 解析各种格式的日期，统一转换为 Date 对象
 * @param {*} timeStr - 日期数据（可以是数组、字符串、对象等）
 * @returns {Date|null} - Date对象，解析失败返回null
 */
function parseDate(timeStr) {
  if (!timeStr) return null
  
  let time
  
  try {
    // 处理数组格式：[2024, 1, 1, 12, 0, 0]
    if (Array.isArray(timeStr)) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = timeStr
      time = new Date(year, month - 1, day, hour, minute, second)
    } 
    // 处理字符串格式
    else if (typeof timeStr === 'string') {
      // 如果包含T，说明是ISO格式：2024-01-01T12:00:00
      if (timeStr.includes('T')) {
        time = new Date(timeStr)
      } 
      // 处理其他格式：2024-01-01 12:00:00
      else {
        time = new Date(timeStr.replace(' ', 'T'))
      }
    } 
    // 处理对象格式（如果后端返回的是对象）
    else if (typeof timeStr === 'object' && timeStr !== null) {
      // 尝试从对象中提取日期信息
      if (timeStr.year && timeStr.month && timeStr.day) {
        time = new Date(
          timeStr.year, 
          timeStr.month - 1, 
          timeStr.day,
          timeStr.hour || 0,
          timeStr.minute || 0,
          timeStr.second || 0
        )
      } else {
        time = new Date(timeStr)
      }
    } 
    else {
      time = new Date(timeStr)
    }
    
    // 检查日期是否有效
    if (isNaN(time.getTime())) {
      console.warn('日期解析失败:', timeStr)
      return null
    }
    
    return time
  } catch (error) {
    console.error('日期解析错误:', timeStr, error)
    return null
  }
}

/**
 * 格式化时间为相对时间（刚刚、X分钟前等）
 * @param {*} timeStr - 日期数据
 * @returns {string} - 格式化后的相对时间字符串
 */
export function formatTime(timeStr) {
  const time = parseDate(timeStr)
  if (!time) return ''
  
  const now = new Date()
  const diff = now - time
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return time.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }
}

/**
 * 格式化日期时间为完整格式（2024-01-01 12:00:00）
 * @param {*} dateStr - 日期数据
 * @returns {string} - 格式化后的日期时间字符串
 */
export function formatDate(dateStr) {
  const date = parseDate(dateStr)
  if (!date) return '-'
  
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}

/**
 * 格式化日期为简短格式（2024-01-01）
 * @param {*} dateStr - 日期数据
 * @returns {string} - 格式化后的日期字符串
 */
export function formatDateShort(dateStr) {
  const date = parseDate(dateStr)
  if (!date) return '-'
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-')
}

