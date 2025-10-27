package com.scarasol.fungalhazard.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

/**
 * @author Scarasol
 */
public interface IJumpZombie {
    boolean canJump();
    void setLastJumpTime(long lastJumpTime);

    default Vec3 getMinimalJumpVec(LivingEntity self, LivingEntity targetEntity, double vMax) {
        // 获取实体重力属性（默认 0.05）
        AttributeInstance gravityAttr = self.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        double g = gravityAttr != null ? gravityAttr.getValue() : 0.05D;
        if (g <= 0.0) {
            return Vec3.ZERO; // 防御：重力为0不处理
        }

        Vec3 start = self.position();
        Vec3 targetVelocity = targetEntity.getDeltaMovement();
        Vec3 target = targetEntity.getEyePosition().add(targetVelocity.x, - self.getBbHeight() * 0.5, targetVelocity.z);
        Vec3 diff = target.subtract(start);

        double dx = diff.x;
        double dy = diff.y;
        double dz = diff.z;

        double d = Math.sqrt(dx * dx + dz * dz);
        if (d < 0.01) {
            return Vec3.ZERO; // 水平距离太小，不计算
        }

        double h = dy;

        // 解析求最小 v^2 = u
        // u = g * ( h + sqrt(h^2 + d^2) )
        double inner = h * h + d * d;
        if (inner < 0) {
            return Vec3.ZERO; // 理论上不会发生
        }
        double u = g * (h + Math.sqrt(inner));
        if (u <= 0) {
            return Vec3.ZERO; // 无正解
        }

        double vMin = Math.sqrt(u);

        // 若最小速度超出允许最大速度，则跳不过去
        if (vMin > vMax) {
            return Vec3.ZERO;
        }

        // 唯一角度（判别式为0时）tanθ = v^2 / (g * d)
        double tanTheta = u / (g * d);
        // 计算 sin, cos
        double cosTheta = 1.0 / Math.sqrt(1.0 + tanTheta * tanTheta);
        double sinTheta = tanTheta * cosTheta;

        if (sinTheta <= 0) {
            return Vec3.ZERO; // 垂直分量必须大于0
        }

        double vxz = vMin * cosTheta;
        double vy = vMin * sinTheta;

        // 水平单位方向向量
        Vec3 dir = new Vec3(dx / d, 0.0, dz / d);

        Vec3 velocity = new Vec3(dir.x * vxz, vy, dir.z * vxz);

        // 保险性检查：长度不超过 vMax（数值误差）
        if (velocity.length() > vMax) {
            return Vec3.ZERO;
        }

        if (velocity.y <= 0) {
            return Vec3.ZERO;
        }

        return velocity;
    }
}
