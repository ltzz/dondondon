package controller;

import java.util.HashSet;

public class BottomForm {
    public static final class FormState {
        private String inReplyToId;
        private String imageId; // TODO: 複数持てるように
        HashSet<String> statusTexts;

        FormState() {
            statusTexts = new HashSet<>();
        }

        public HashSet<String> getStatusTexts() {
            return statusTexts;
        }

        public String getImageId() {
            return imageId;
        }

        public String getInReplyToId() {
            return inReplyToId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        public void setInReplyToId(String inReplyToId) {
            this.inReplyToId = inReplyToId;
        }

        public String getStatusDisplayText(){
            return String.join("/", getStatusTexts());
        }

        public void initialize() {
            statusTexts = new HashSet<>();
            this.imageId = null;
            this.inReplyToId = null;
        }
    }
}
