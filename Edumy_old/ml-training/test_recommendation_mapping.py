import os
import json
import unittest
from MLService.services.recommendation_mapping_service import RecommendationMappingService

class TestRecommendationMapping(unittest.TestCase):
    def setUp(self):
        # Create a valid temp config mapping file
        self.temp_config_path = "ml-training/scratch/temp_mapping.json"
        os.makedirs(os.path.dirname(self.temp_config_path), exist_ok=True)
        self.valid_data = {
            "version": "1.0.0",
            "mappings": [
                {"modelItemId": "AAA", "courseId": 1},
                {"modelItemId": "BBB", "courseId": 2},
                {"modelItemId": "CCC", "courseId": 3},
                {"modelItemId": "DDD", "courseId": 4}
            ]
        }
        with open(self.temp_config_path, "w", encoding="utf-8") as f:
            json.dump(self.valid_data, f)

    def tearDown(self):
        if os.path.exists(self.temp_config_path):
            os.remove(self.temp_config_path)

    def test_valid_mappings(self):
        # Test CCC and DDD map to correct database CourseId
        service = RecommendationMappingService(self.temp_config_path)
        self.assertEqual(service.get_course_id("CCC"), 3)
        self.assertEqual(service.get_course_id("DDD"), 4)

    def test_bidirectional_conversion(self):
        # Test conversion in both directions
        service = RecommendationMappingService(self.temp_config_path)
        self.assertEqual(service.get_course_id("AAA"), 1)
        self.assertEqual(service.get_model_item_id(1), "AAA")

    def test_unknown_model_item(self):
        # Test unknown model item raises KeyError
        service = RecommendationMappingService(self.temp_config_path)
        with self.assertRaises(KeyError):
            service.get_course_id("XYZ")

    def test_unknown_course_id(self):
        # Test unknown course ID raises KeyError
        service = RecommendationMappingService(self.temp_config_path)
        with self.assertRaises(KeyError):
            service.get_model_item_id(999)

    def test_duplicate_model_item(self):
        # Test duplicate modelItemId raises ValueError
        dup_data = {
            "version": "1.0.0",
            "mappings": [
                {"modelItemId": "CCC", "courseId": 3},
                {"modelItemId": "CCC", "courseId": 4}
            ]
        }
        dup_path = "ml-training/scratch/dup_model.json"
        with open(dup_path, "w") as f:
            json.dump(dup_data, f)
        
        with self.assertRaises(ValueError):
            RecommendationMappingService(dup_path)
        os.remove(dup_path)

    def test_duplicate_course_id(self):
        # Test duplicate courseId raises ValueError
        dup_data = {
            "version": "1.0.0",
            "mappings": [
                {"modelItemId": "CCC", "courseId": 3},
                {"modelItemId": "DDD", "courseId": 3}
            ]
        }
        dup_path = "ml-training/scratch/dup_course.json"
        with open(dup_path, "w") as f:
            json.dump(dup_data, f)
        
        with self.assertRaises(ValueError):
            RecommendationMappingService(dup_path)
        os.remove(dup_path)

    def test_missing_mapping_file(self):
        # Test loading missing file raises FileNotFoundError
        with self.assertRaises(FileNotFoundError):
            RecommendationMappingService("nonexistent_file.json")

    def test_invalid_json(self):
        # Test loading invalid JSON raises exception
        invalid_path = "ml-training/scratch/invalid.json"
        with open(invalid_path, "w") as f:
            f.write("{invalid: json")
        
        with self.assertRaises(Exception):
            RecommendationMappingService(invalid_path)
        os.remove(invalid_path)

if __name__ == "__main__":
    unittest.main()
