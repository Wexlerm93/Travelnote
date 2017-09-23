package de.ur.mi.travelnote;


public class CustomImage {

        private int id;
        private String title, location;
        private byte[] image;

        public CustomImage(String title, String location, byte[] image) {
            this.title = title;
            this.location = location;
            this.image = image;
            //this.id = id;
        }

        /*
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        */

        public String getTitle() {
            return title;
        }



        public String getLocation() {
            return location;
        }



        public byte[] getImage() {
            return image;
        }


}
