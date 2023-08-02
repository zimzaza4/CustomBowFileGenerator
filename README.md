# CustomBowFileGenerator

要求: Java16+ 使用方法: java -jar CustomBowFileGenerator.jar <命名空间>
确保与以下文件存在
- original_item_texture.json (你原本资源包的item_texture) 
- template.json (弓attachable模板) 
- player.animation_controllers.json bows/ (贴图文件夹) 
- 其中bows文件夹格式: bows/<bow的id>/所有贴图
- 将render_controller.zip的内容添加至包中

贴图命名规范: 
- xxxx_standby (没有拉动的材质)
- xxxx_pulling_0 ~ 2
