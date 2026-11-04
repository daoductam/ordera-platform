import type { Restaurant, MenuItem, OrderOptions } from "@/types/store-discovery";

export const DEFAULT_ORDER_OPTIONS: OrderOptions = {
  method: "Delivery",
  timing: "asap",
  locationName: "TeaOlogy Posthallen",
  locationAddress: "Prinsens gate 10C, 0153 Oslo, Norway",
};

export const MOCK_RESTAURANTS: Restaurant[] = [
  {
    id: "shop-1",
    name: "Shop Name 1",
    address: "Shop Address",
    openHours: "10:30 AM - 10:00 PM",
    methods: ["Dine In", "Delivery", "Takeaway"],
    preparingTime: "15 Mintues",
    image: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&q=80", // Fried chicken burger hero matching Figma 1.6!
    isFavorite: false,
  },
  {
    id: "shop-2",
    name: "Linn Sushi Sentrum",
    address: "Prinsens gate 10C, 0153 Oslo",
    openHours: "10:30 AM - 10:00 PM",
    methods: ["Takeaway", "Dine In", "Delivery"],
    preparingTime: "15 Min",
    image: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800&q=80",
    isFavorite: true,
  },
  {
    id: "shop-3",
    name: "TeaOlogy Posthallen",
    address: "Tollbugata 8A, 0152 Oslo, Norway",
    openHours: "9:00 - 23:00",
    methods: ["Takeaway", "Dine In", "Delivery"],
    preparingTime: "10-15 Min",
    image: "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=800&q=80",
    isFavorite: false,
  },
];

export const MOCK_MENU_ITEMS: MenuItem[] = [
  {
    id: "maki-1",
    name: "Laks Maki",
    description: "8 biter maki med laks og kremost. Velg mellom mango eller avokado ...eller avokado ...eller avokadoavokadoavokadoavokado avokado ...",
    price: 150.00,
    category: "Popular",
    subCategory: "Fried maki",
    pieces: "8 biter",
    tags: ["Beef", "Gluten Free", "Organic", "Spicy", "Popular"],
    isPopular: true,
    image: "https://images.unsplash.com/photo-1558857563-b37cf5a983b6?w=400&q=80", // Dark tapioca/dessert bowl in white ceramic bowl matching Figma 1.6!
  },
  {
    id: "maki-2",
    name: "Tuna Maki",
    description: "8 biter maki med tuna og kremost. Velg mellom mango eller avokado ...8 biter maki med tuna og kremost ...",
    price: 111.20,
    originalPrice: 130.00,
    category: "Popular",
    subCategory: "Fried maki",
    pieces: "8 biter",
    tags: ["Beef", "Gluten Free", "Organic", "Spicy", "Popular"],
    isPopular: true,
    image: "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&q=80", // Colorful cubes sushi dish matching Figma 1.6!
  },
  {
    id: "maki-3",
    name: "Tempura Scampi Maki",
    description: "Frityrstekt scampi med avokado, teriyakisaus og ristede sesamfrø.",
    price: 125.00,
    originalPrice: 145.00,
    category: "Maki",
    subCategory: "Fried maki",
    pieces: "8 biter",
    tags: ["Popular"],
    image: "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=400&q=80",
  },
  {
    id: "nigiri-1",
    name: "Laks & Flamberet Laks Nigiri",
    description: "4 biter rå laks nigiri og 4 biter flamberet laks med teriyakisaus.",
    price: 135.00,
    category: "Nigiri",
    pieces: "8 biter",
    tags: ["Gluten Free"],
    image: "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=400&q=80",
  },
  {
    id: "drink-1",
    name: "Classic Brown Sugar Milk Tea",
    description: "TeaOlogy signature melkete med håndlaget brun sukker og tapiokaperler.",
    price: 68.00,
    category: "Drinks",
    tags: ["Organic", "Popular"],
    image: "https://images.unsplash.com/photo-1558857563-b37cf5a983b6?w=400&q=80",
  },
  {
    id: "hot-1",
    name: "Koreansk Crispy Kylling Burger",
    description: "Sprøstekt kyllingfilet marinert i gochujang med kimchi coleslaw i briochebrød.",
    price: 149.00,
    category: "Hot Food",
    tags: ["Spicy", "Popular"],
    image: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80",
  },
];

