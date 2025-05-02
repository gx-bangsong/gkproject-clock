package com.gkprojct.clock.vm // <-- **确保包路径正确**

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Import TypeConverters annotation

// --- 导入 RuleEntity 和 RuleConverters (从同一个包导入) ---
import com.gkprojct.clock.vm.RuleEntity
import com.gkprojct.clock.vm.RuleConverters
// --------------------------------------------------------

// 将 RuleEntity 添加到 entities 列表中
// version 是数据库的版本号，每次修改数据库结构（添加/删除表、添加/删除字段等）都需要升级版本号
@Database(entities = [RuleEntity::class], version = 1, exportSchema = false)
// 注册 TypeConverters 类，让 Room 知道如何处理自定义类型
@TypeConverters(RuleConverters::class) // <-- **使用 RuleConverters**
abstract class AppDatabase : RoomDatabase() {

    // 定义获取 RuleDao 的抽象方法
    abstract fun ruleDao(): RuleDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile // Volatile ensures that writes to a variable are immediately visible to other threads.
        private var Instance: AppDatabase? = null

        // 获取数据库实例的函数
        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, then return it,
            // if it is, then create the database
            return Instance ?: synchronized(this) { // synchronized ensures that only one thread can access the block at a time
                Room.databaseBuilder(
                    context.applicationContext, // 使用 applicationContext 防止内存泄漏
                    AppDatabase::class.java,
                    "app_database" // 数据库文件名
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not covered in this example.
                    // In a real app, you should implement migrations to handle database schema changes.
                    .fallbackToDestructiveMigration() // 数据库版本升级时，如果缺少迁移策略，则销毁重建数据库 (开发时方便，生产环境不推荐)
                    .build()
                    .also { Instance = it } // 将创建的数据库实例赋值给 Instance
            }
        }
    }
}
