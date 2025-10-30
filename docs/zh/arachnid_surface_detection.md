# 爬行表面检测方法详解

以下内容翻译自先前对 `getBlockCollisions`、`detectBlockingSurface`、`selectAttachmentDirection` 和 `hasSufficientOverlap` 方法的英文说明，并补充了必要的上下文，帮助你理解真菌蜘蛛僵尸如何判定贴附的表面。

## `Level#getBlockCollisions`
- `level().getBlockCollisions(this, shrunkenBox.inflate(SURFACE_CHECK_STEP))` 会向当前世界请求：在实体收缩后的包围盒四周（额外扩展 0.25 格的“触须”范围内），有哪些方块的碰撞体会与之相交。
- 第一个参数 `this` 让碰撞查询能够结合实体自身的体积进行计算；第二个参数则是需要扫描的空间范围。
- 该方法返回一个 `Iterable<VoxelShape>`，其中每个 `VoxelShape` 都代表某个方块提供的碰撞几何体，也就是会实际阻挡实体的体积。

## `detectBlockingSurface`
1. **准备检测体积**：先把实体包围盒向内收缩 0.02 格，避免把自身的体积算进去；随后向四周再扩展 `SURFACE_CHECK_STEP`（0.25 格），只扫描贴身的方块。
2. **遍历碰撞体**：对 `getBlockCollisions` 返回的每个 `VoxelShape` 做循环。若碰撞体为空则跳过，否则调用 `shape.toAabbs()` 把它拆成一个个方块级的轴对齐包围盒逐一处理。
3. **选出候选朝向**：通过 `selectAttachmentDirection` 推断实体正在接触方块的哪一个面。如果得到的是 `DOWN`（实体脚下）或 `null`，就跳过，不把地面视为新的附着面。
4. **确认接触质量**：`hasSufficientOverlap` 会检查实体与方块在法线垂直的另外两个轴上是否有足够的交叠，从而确保实体是真的沿着该面滑动，而不是只碰到了角落。
5. **按间隙打分**：`computeAttachmentScore` 计算实体与方块之间沿着附着方向的缝隙。距离越小分数越高，超过 0.25 格的都会被判为 0 分。若得分为正且方向与之前记录的 `preferred` 一致，就立即复用旧方向保持稳定；否则保留分数最高的方向作为新的候选。
6. **返回结果**：遍历结束后，返回分数最高的那个方向（若不存在则返回 `null`），作为新的 `crawlingDirection`。

## `selectAttachmentDirection`
- 该方法取实体（收缩后的包围盒）与方块包围盒的中心点，计算两者的差向量。
- 通过 `Direction.getNearest` 把这个差向量映射为最接近的六个基本方向之一（±X、±Y、±Z）。
- 得到的方向指示实体最有可能贴附的方块面。

## `hasSufficientOverlap`
- 在选定了附着方向之后，需要确认实体与方块在其他两个轴向上存在正的交叠距离。
- 方法会根据方向选择对应的轴对组合，并调用 `overlap` 判断区间交叠是否超过 `CONTACT_EPSILON`（1e-3）。
- 只有两个轴都满足交叠条件，才认为实体真正“贴”在该面上，而非只是擦到边缘。

## 小结
通过上述流程，真菌蜘蛛僵尸能够在任意会阻挡它的方块表面上寻找最佳附着方向，并在保持稳定性的同时，实现全方位的爬行能力。
