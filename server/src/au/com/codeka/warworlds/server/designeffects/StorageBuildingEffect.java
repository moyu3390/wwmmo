package au.com.codeka.warworlds.server.designeffects;

import org.w3c.dom.Element;

import au.com.codeka.common.model.BaseBuilding;
import au.com.codeka.common.model.BaseColony;
import au.com.codeka.common.model.BaseEmpirePresence;
import au.com.codeka.common.model.BaseStar;
import au.com.codeka.common.model.BuildingEffect;
import au.com.codeka.warworlds.server.model.Colony;
import au.com.codeka.warworlds.server.model.EmpirePresence;

public class StorageBuildingEffect extends BuildingEffect {
  private float goods;
  private float minerals;

  @Override
  public void load(Element effectElement) {
    goods = Float.parseFloat(effectElement.getAttribute("goods"));
    minerals = Float.parseFloat(effectElement.getAttribute("minerals"));
  }

  @Override
  public void apply(BaseStar star, BaseColony baseColony, BaseBuilding building) {
    Colony colony = (Colony) baseColony;
    for (BaseEmpirePresence baseEmpirePresence : star.getEmpirePresences()) {
      EmpirePresence empirePresence = (EmpirePresence) baseEmpirePresence;
      if (empirePresence == null || colony.getEmpireID() == null) {
        return;
      }
      if (empirePresence.getEmpireID() == colony.getEmpireID()) {
        empirePresence.setMaxGoods(empirePresence.getMaxGoods() + goods);
        empirePresence.setMaxMinerals(empirePresence.getMaxMinerals() + minerals);
      }
    }
  }
}
