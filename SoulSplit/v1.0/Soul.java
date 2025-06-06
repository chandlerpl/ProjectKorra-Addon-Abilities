package Hiro3;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;

public class Soul {

	private Ability ability;
	
	private Player owner;
	private Location loc;
	private double hearts;
	private double health;
	private Vector dir;
	public long startTime;
	private long soulWaitTime;
	private String eyeColor;
	private boolean isAlive;
	private Location eyeLocLeft;
	private Location eyeLocRight;
	
	private boolean isMelding;
	private int meldFlag;
	
	private boolean isStackable;
	
	private double soulThrowSpeed;
	
	private boolean isSelected;
	private int selectFlag;
	
	public Soul(Ability ability, Player owner, Location loc, double hearts, double health) {
		this.ability = ability;
		this.owner = owner;
		this.dir = owner.getLocation().getDirection().clone();
		if (dir.getY() <= -0.93 || dir.getY() >= 0.93) {
			dir.setX(1);
		}
		this.loc = loc;
		this.hearts = hearts;
		this.setHealth(health);
		this.startTime = System.currentTimeMillis();
		this.soulWaitTime = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.InactiveSoulTime");
		this.eyeColor = "9ECCFF";
		setAlive(true);
		isMelding = false;
		meldFlag = 0;
		isStackable = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Spirit.SoulSplit.isStackable");
		soulThrowSpeed = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.SoulSplit.SoulThrowSpeed");
		
		isSelected = false;
		selectFlag = 0;
	}
	
	public void display() {
		Location tmpLoc = getLoc().clone();
		Vector rightTmpVec = new Vector(-dir.getZ(), 0, +dir.getX());
		rightTmpVec.normalize();
		rightTmpVec.multiply(0.40);
		Vector leftTmpVec = rightTmpVec.clone().multiply(-1);
		
		Location tmpLocLeft = tmpLoc.clone();
		tmpLocLeft.add(leftTmpVec);
		Location tmpLocRight = tmpLoc.clone();
		tmpLocRight.add(rightTmpVec);
		
		if (isMelding && meldFlag == 0) {
			meldFlag = 1;
			displayMeldCircle();
		}

		if (isSelected && selectFlag == 0) {
			selectFlag = 1;
			displaySelectSpiral(2);
		}
		
		for (double i = 0; i < 1.8; i+=0.2) {
			
			if (isSelected()) {
				
			}
			
			if (isMelding()) {
				owner.spawnParticle(Particle.CLOUD, tmpLoc, 0);
				if (i >= 1 && i <= 1.2) {
					owner.spawnParticle(Particle.CLOUD, tmpLocLeft, 0);
					owner.spawnParticle(Particle.CLOUD, tmpLocRight, 0);
				}
				if (i == 1.4) {
					Location l = tmpLoc.clone().add(dir.clone().setY(0).multiply(0.2));
					eyeLocLeft = l.clone().add(leftTmpVec.getX() * 0.4, 0.4, leftTmpVec.getZ() * 0.4);
					eyeLocRight = l.clone().add(rightTmpVec.getX() * 0.4, 0.4, rightTmpVec.getZ() * 0.4);
					owner.spawnParticle(Particle.DUST, eyeLocLeft.getX(), eyeLocLeft.getY(), eyeLocLeft.getZ(), 0, 0.5, 0.5, 0.5, 1);
					owner.spawnParticle(Particle.DUST, eyeLocRight.getX(), eyeLocRight.getY(), eyeLocRight.getZ(), 0, 0.5, 0.5, 0.5, 1);
				}
			} else {
				owner.getWorld().spawnParticle(Particle.CLOUD, tmpLoc, 0);
				if (i >= 1 && i <= 1.2) {
					owner.getWorld().spawnParticle(Particle.CLOUD, tmpLocLeft, 0);
					owner.getWorld().spawnParticle(Particle.CLOUD, tmpLocRight, 0);
				}
				if (i == 1.4) {
					Location l = tmpLoc.clone().add(dir.clone().setY(0).multiply(0.2));
					eyeLocLeft = l.clone().add(leftTmpVec.getX() * 0.4, 0.4, leftTmpVec.getZ() * 0.4);
					eyeLocRight = l.clone().add(rightTmpVec.getX() * 0.4, 0.4, rightTmpVec.getZ() * 0.4);
					GeneralMethods.displayColoredParticle(eyeLocLeft, eyeColor, 0, 0, 0);
					GeneralMethods.displayColoredParticle(eyeLocRight, eyeColor, 0, 0, 0);
				}
			}
			tmpLoc.add(0, 0.2, 0);
			tmpLocLeft.add(0, 0.2, 0);
			tmpLocRight.add(0, 0.2, 0);
		}
	}
	
	public void moveSoul(double distance) {
		new BukkitRunnable() {
			
			Location startLoc = getLoc().clone();
			
			@Override
			public void run() {
				if (!isAlive() || startLoc.distance(getLoc()) > distance) {
					Bukkit.getScheduler().cancelTask(getTaskId());
				} else {
					setLoc(getLoc().clone().add(getDir().clone().setY(0).multiply(soulThrowSpeed)));
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public void moveSoul(Vector dir, Location targetLoc) {
		new BukkitRunnable() {
			
			Location startLoc = getLoc().clone();
			
			@Override
			public void run() {
				if (!isAlive() || startLoc.distance(getLoc()) > startLoc.distance(targetLoc)) {
					Bukkit.getScheduler().cancelTask(getTaskId());
				} else {
					setLoc(getLoc().clone().add(dir.clone().multiply(soulThrowSpeed)));
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public void changeDirectionWithAnimation(Vector targetDirection) {
		new BukkitRunnable() {
			
			Vector dir = getDir().clone();
			double maxAngle = angleBetweenTwoVectors(dir, targetDirection);
			double currentAngle = 0;
			double iterateAngle = (maxAngle / 20);
			int flag = 0;
			
			@Override
			public void run() {
				if (!isAlive() || currentAngle > maxAngle) {
					Bukkit.getScheduler().cancelTask(getTaskId());
				} else {
					currentAngle += (maxAngle / 20);
					setDir(rotateVectorAroundY(getDir(), iterateAngle));
					if (flag == 0 && angleBetweenTwoVectors(getDir(), targetDirection) > maxAngle) {
						iterateAngle = -iterateAngle;
						flag++;
					}
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	@SuppressWarnings("deprecation")
	public boolean hitSoul(Player target) {
		if (target.getUniqueId() != owner.getUniqueId() && System.currentTimeMillis() > getStartTime() + soulWaitTime) {
			setAlive(false);
			owner.setHealthScale(owner.getHealthScale() + getHearts());
			owner.setMaxHealth(owner.getMaxHealth() + getHearts());
			owner.setHealth(owner.getHealth() + getHealth());
			//A solution to damage doesn't stack problem.
			if (isStackable) {
			target.damage(0.01);
				if (target.getHealth() - getHearts() < 0)
					target.setHealth(0);
				else
					target.setHealth(target.getHealth() - getHearts());
			} else {
				DamageHandler.damageEntity(target, getHearts(), this.ability);
			}
			target.getWorld().playSound(getLoc(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 10);
			PotionEffect pe = new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 1);
  			owner.addPotionEffect(pe);
			explode(target.getWorld(), getLoc(), Particle.CLOUD);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean collectSoul() {
  		if (owner.getLocation().getWorld().equals(getLoc().getWorld()) && owner.getLocation().distance(getLoc()) < 1 && System.currentTimeMillis() > getStartTime() + soulWaitTime) {
  			setAlive(false);
  			owner.setHealthScale(owner.getHealthScale() + getHearts());
  			owner.setMaxHealth(owner.getMaxHealth() + getHearts());
  			if (owner.getHealth() != 0)
  				owner.setHealth(owner.getHealth() + getHealth());
  			//player.getWorld().playSound(s.getLoc(), Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR, 1, 10);
  			owner.getWorld().playSound(getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
  			PotionEffect pe = new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 1);
  			owner.addPotionEffect(pe);
  			return true;
  		}
  		return false;
    }
	
	public void explode(World world, Location center, Particle particle) {
		new BukkitRunnable() {
    	  
			Location loc = center.clone();
			double radius = 2.0;
			double tmpRadius;
			int count = 1;
			
			public void run() {
				tmpRadius = 0.0;
				for (double i = -0.5 * count; i <= 0.5 * count; i += 0.5) {
					for (int j = 0; j <= 360; j += 30) {
						double angle = Math.toRadians(j);
						loc.setX(center.getX() + tmpRadius * Math.cos(angle));
						loc.setZ(center.getZ() + tmpRadius * Math.sin(angle));
						loc.setY(center.getY() + i);
						world.spawnParticle(particle, loc, 0);
					}
					if (i < 0) {
						tmpRadius += radius / (1 * count) * -i / 5;
					} else {
						tmpRadius -= radius / (1 * count) * i / 5;
					}
				}
				radius += 1;
				count += 1;
				if (count > 8) {
					Bukkit.getScheduler().cancelTask(getTaskId());
				}
			}  
		}.runTaskTimer(ProjectKorra.plugin, 0, 0);
    }
	
	public void displayMeldCircle() {		
		new BukkitRunnable() {
			
			int phase = 0;
			
			@Override
			public void run() {
				if (!isAlive || !isMelding()) {
					Bukkit.getScheduler().cancelTask(getTaskId());
				} else {
					for(int i = phase; i <= 360 + phase; i+=40) {
						double angle = Math.toRadians(i);
						Location loc = getLoc().clone().add(0, 1, 0);
						loc.setX(loc.getX() + 1.5 * Math.cos(angle));
						loc.setZ(loc.getZ() + 1.5 * Math.sin(angle));
						owner.spawnParticle(Particle.DUST, loc.getX(), loc.getY(), loc.getZ(), 0, 0.5, 0.5, 0.5, 1);
					}
					phase += 2;
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public void displaySelectSpiral(double maxHeight) {
		new BukkitRunnable() {
			
			double radius = 0.75;
			HashMap<Integer, Double> heights = new HashMap<Integer, Double>();
			HashMap<Integer, Integer> signs = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> phases = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> angles = new HashMap<Integer, Integer>();
			int flag = 0;
			
			@Override
			public void run() {
				if (!isAlive() || !isSelected()) {
					Bukkit.getScheduler().cancelTask(getTaskId());
					setSelectFlag(0);
				} else if (flag == 0) {
					for (int i = 0; i < 4; i++) {
						heights.put(i, Math.random() * 2);
						signs.put(i, Math.random() < 0.5 ? 1 : -1);
						phases.put(i, Math.random() < 0.5 ? (int) (Math.random() * 5 - 10) : (int) (Math.random() * 5 + 5));
						angles.put(i, 0);
					}
					flag++;
				} else {
					for (int i = 0; i < 4; i++) {
						double angle = Math.toRadians(angles.get(i));
						Location loc = getLoc().clone().add(0, heights.get(i), 0);
						loc.setX(loc.getX() + radius * Math.cos(angle));
						loc.setZ(loc.getZ() + radius * Math.sin(angle));
						owner.spawnParticle(Particle.DUST, loc, 0, -1, 0.9, 0);
					}
					
					for (int i = 0; i < 4; i++) {
						if (heights.get(i) > maxHeight) {
							signs.remove(i);
							signs.put(i, -1);
						} else if (heights.get(i) <= 0) {
							signs.remove(i);
							signs.put(i, 1);
						}
						double tmp = heights.get(i);
						heights.remove(i);
						heights.put(i, tmp + (maxHeight / 40) * signs.get(i));
						int tmp1 = angles.get(i);
						angles.remove(i);
						angles.put(i, tmp1 + phases.get(i));
						
					}
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public static Vector rotateVectorAroundY(Vector vector, double degrees) {
        double rad = Math.toRadians(degrees);
       
        double currentX = vector.getX();
        double currentZ = vector.getZ();
       
        double cosine = Math.cos(rad);
        double sine = Math.sin(rad);
       
        return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
    }
	
	public double angleBetweenTwoVectors(Vector v1, Vector v2) {
        double angle = Math.acos( v1.dot(v2)  /  (v1.length() * v2.length()) );
        angle = Math.toDegrees(angle);
        
        return angle;
	}
	
	public Location getEyeLocLeft() {
		return eyeLocLeft;
	}

	public Location getEyeLocRight() {
		return eyeLocRight;
	}

	public Player getOwner() {
		return owner;
	}
	
	public void setOwner(Player owner) {
		this.owner = owner;
	}
	
	public Location getLoc() {
		return loc;
	}
	
	public void setLoc(Location loc) {
		this.loc = loc;
	}
	
	public double getHearts() {
		return hearts;
	}
	
	public void setHearts(double hearts) {
		this.hearts = hearts;
	}
	
	public void setSoulWaitTime(long time) {
		this.soulWaitTime = time;
	}
	
	public long getStartTime() {
		return this.startTime;
	}
	
	public String getEyeColor() {
		return eyeColor;
	}
	
	public Vector getDir() {
		return dir;
	}

	public void setDir(Vector dir) {
		this.dir = dir;
	}

	public void setEyeColor(String eyeColor) {
		this.eyeColor = eyeColor;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}
	
	public boolean isMelding() {
		return isMelding;
	}
	
	public void setIsMelding(boolean value) {
		isMelding = value;
	}
	
	public boolean isSelected() {
		return this.isSelected;
	}
	
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public void setSelectFlag(int flag) {
		this.selectFlag = flag;
	}
}

