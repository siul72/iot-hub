package co.luism.diagnostics.common;

import co.luism.diagnostics.enterprise.Vehicle;

import java.util.EventObject;

/**
 * Created by luis on 14.11.14.
 */
public class VehicleEvent extends EventObject {

        private static final long serialVersionUID = 1L;
        private final Vehicle currentVehicle;
        public VehicleEvent(Vehicle source) {
            super(source);
            this.currentVehicle = source;
        }
        public Vehicle getCurrentVehicle() {
            return currentVehicle;
        }

}

