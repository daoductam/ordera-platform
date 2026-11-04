export type OrderMethod = "Dine In" | "Takeaway" | "Delivery";
export type OrderTiming = "asap" | "schedule";

export interface OrderOptions {
  method: OrderMethod;
  timing: OrderTiming;
  scheduledTime?: string;
  locationName: string;
  locationAddress: string;
}

export interface Restaurant {
  id: string;
  name: string;
  address: string;
  openHours: string;
  methods: OrderMethod[];
  preparingTime: string;
  image: string;
  isFavorite?: boolean;
}

export interface MenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  category: string;
  subCategory?: string;
  image: string;
  tags?: ("Beef" | "Gluten Free" | "Organic" | "Spicy" | "% Discount" | "Popular")[];
  isPopular?: boolean;
  pieces?: string;
}

