package me.hiro3.firespray;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class FireSpray extends FireAbility implements AddonAbility {
	
	private Listener FSL;
	
	private Location tmp;
	
	private long cooldown;
	private int tick;
	private int maximumTick;
	private ArrayList<Boid> boids;
	private int amount;
	private double damage;
	private double blueFireDamage;
	private double range;
	
	public FireSpray(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this))
			return;
		
		if (hasAbility(player, FireSpray.class))
			return;
		
		if (player.getLocation().getBlock().isLiquid())
			return;
		
		setField();
		start();
	}
	
	public void setField() {
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Fire.FireSpray.Cooldown");
		this.tick = 1;
		this.boids = new ArrayList<Boid>();
		this.maximumTick = (int) (ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Fire.FireSpray.Duration") / 50);
		this.range = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.FireSpray.Range");
		this.damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.FireSpray.Damage");
		this.blueFireDamage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.FireSpray.BlueFireDamage");
		this.amount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Fire.FireSpray.ParticlePerTick");
		this.amount = 2;
		
		PotionEffect pe = new PotionEffect(PotionEffectType.MINING_FATIGUE, maximumTick, 100);
		player.addPotionEffect(pe);
		
		if (player.getMainHand().equals(MainHand.RIGHT)) {
			tmp = getRightSide(player.getLocation().add(0, 1, 0), 0.2);
		} else {
			tmp = getLeftSide(player.getLocation().add(0, 1, 0), 0.2);
		}
		
		for (int i = 0; i < this.amount; i++) {
			boids.add(new Boid(player, this, tmp.clone().add(Math.random() * 0.5 - 0.25, Math.random() * 0.5 - 0.25, Math.random() * 0.5 - 0.25), player.getLocation().getDirection()
					, this.damage, this.blueFireDamage, this.range));
		}
	}

	@Override
	public void progress() {
		if (boids.isEmpty()) {
			remove();
			if (tick <= maximumTick) {
				bPlayer.addCooldown(this);
				player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
			}
			return;
		}
		
		if (tick <= maximumTick) {
			if (tick == maximumTick) {
				bPlayer.addCooldown(this);
				player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
			}
			
			if (!player.getLocation().getBlock().isLiquid()) {
				if (tick % 5 == 0) {
					FireAbility.playFirebendingSound(player.getLocation());
				}
				
				for (int i = 0; i < this.amount; i++) {
					if (player.getMainHand().equals(MainHand.RIGHT)) {
						tmp = getRightSide(player.getLocation().add(0, 1, 0), 0.2);
					} else {
						tmp = getLeftSide(player.getLocation().add(0, 1, 0), 0.2);
					}
					boids.add(new Boid(player, this, tmp.clone().add(Math.random() * 0.5 - 0.25, Math.random() * 0.5 - 0.25, Math.random() * 0.5 - 0.25), player.getLocation().getDirection()
							, this.damage, this.blueFireDamage, this.range));
				}
			}
		}
		
		for (Boid b : boids) {
			b.update();
			b.show();
		}
		
		for (int i = boids.size()-1; i >= 0; i--) {
			if (boids.get(i).getTicksLived() > this.maximumTick)
				boids.remove(boids.get(i));
		}
		
		tick++;
	}
	
	public Location getRightSide(Location location, double distance) {
		Vector dir = player.getLocation().getDirection();
		Vector rightTmpVec = new Vector(-dir.getZ(), 0, +dir.getX());
		rightTmpVec.normalize();
		rightTmpVec.multiply(distance);
		return location.clone().add(rightTmpVec);
	}
	
	public Location getLeftSide(Location location, double distance) {
		Vector dir = player.getLocation().getDirection();
		Vector leftTmpVec = new Vector(+dir.getZ(), 0, -dir.getX());
		leftTmpVec.normalize();
		leftTmpVec.multiply(distance);
		return location.clone().add(leftTmpVec);
	}
	
	public ArrayList<Boid> getBoids() {
		return this.boids;
	}
	
	public int getMaximumTick() {
		return this.maximumTick;
	}
	
	public void setTick(int tick) {
		this.tick = tick;
	}
	
	public int getTick() {
		return this.tick;
	}
	
	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "FireSpray";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Release a spray of fire that can bounce from terrain.";
	}

	@Override
	public String getInstructions() {
		return "Left Click to use.\nSneak + Left Click to end early.";
	}
	
	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "3.0";
	}

	@Override
	public void load() {
		FSL = new FireSprayListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(FSL, ProjectKorra.plugin);
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.FireSpray.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.FireSpray.Duration", 3000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.FireSpray.Range", 18);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.FireSpray.Damage", 1);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.FireSpray.BlueFireDamage", 1);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.FireSpray.ParticlePerTick", 2);
		ConfigManager.defaultConfig.save();
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(FSL);
		super.remove();
	}

}