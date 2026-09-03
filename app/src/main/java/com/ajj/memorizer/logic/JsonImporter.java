package com.ajj.memorizer.logic;

import com.ajj.memorizer.data.AppDatabase;
import com.ajj.memorizer.data.Category;
import com.ajj.memorizer.data.CategoryDao;
import com.ajj.memorizer.data.Flashcard;
import com.ajj.memorizer.data.FlashcardDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonImporter {

    public static void importHierarchicalJson(AppDatabase db, String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);
        CategoryDao categoryDao = db.categoryDao();
        FlashcardDao flashcardDao = db.flashcardDao();

        if (root.has("subject")) {
            JSONObject subjectObj = root.getJSONObject("subject");
            processCategory(categoryDao, flashcardDao, subjectObj, null);
        } else if (root.has("chapter")) {
            // Legacy support or direct chapter import
            processCategory(categoryDao, flashcardDao, root.getJSONObject("chapter"), null);
        }
    }

    private static void processCategory(CategoryDao categoryDao, FlashcardDao flashcardDao, JSONObject obj, Integer parentId) throws JSONException {
        String name = obj.getString("title");
        Category category = categoryDao.getCategoryByName(name, parentId);
        int categoryId;
        if (category == null) {
            category = new Category(name, parentId);
            categoryId = (int) categoryDao.insert(category);
        } else {
            categoryId = category.getId();
        }

        // Process sub-categories (Topics/Chapters)
        if (obj.has("chapters")) {
            JSONArray chapters = obj.getJSONArray("chapters");
            for (int i = 0; i < chapters.length(); i++) {
                processCategory(categoryDao, flashcardDao, chapters.getJSONObject(i), categoryId);
            }
        }
        if (obj.has("topics")) {
            JSONArray topics = obj.getJSONArray("topics");
            for (int i = 0; i < topics.length(); i++) {
                processCategory(categoryDao, flashcardDao, topics.getJSONObject(i), categoryId);
            }
        }

        // Process Flashcards in this category
        parseCardsForCategory(flashcardDao, obj, categoryId);
    }

    private static void parseCardsForCategory(FlashcardDao flashcardDao, JSONObject chapter, int categoryId) throws JSONException {
        // 1. Definitions
        if (chapter.has("definitions")) {
            JSONArray defs = chapter.getJSONArray("definitions");
            for (int i = 0; i < defs.length(); i++) {
                JSONObject obj = defs.getJSONObject(i);
                String q = obj.getString("term");
                String a = obj.getString("definition");
                if (obj.has("notes")) a += "\n\nNote: " + obj.getString("notes");
                flashcardDao.insert(new Flashcard(q, a, categoryId));
            }
        }

        // 2. Phenomena Explained
        if (chapter.has("phenomena_explained")) {
            JSONObject phen = chapter.getJSONObject("phenomena_explained");
            String q = phen.getString("question");
            JSONArray points = phen.getJSONArray("points");
            StringBuilder a = new StringBuilder();
            for (int i = 0; i < points.length(); i++) a.append("- ").append(points.getString(i)).append("\n");
            flashcardDao.insert(new Flashcard(q, a.toString().trim(), categoryId));
        }

        // 3. Energy Types
        if (chapter.has("energy_types")) {
            JSONArray types = chapter.getJSONArray("energy_types");
            for (int i = 0; i < types.length(); i++) {
                JSONObject obj = types.getJSONObject(i);
                flashcardDao.insert(new Flashcard(obj.getString("type"), obj.getString("definition"), categoryId));
            }
        }

        // 4. Energy Units and Formulas
        if (chapter.has("energy_units_and_formulas")) {
            JSONObject unitsObj = chapter.getJSONObject("energy_units_and_formulas");
            if (unitsObj.has("units")) {
                JSONArray units = unitsObj.getJSONArray("units");
                for (int i = 0; i < units.length(); i++) {
                    JSONObject obj = units.getJSONObject(i);
                    String q = obj.getString("name") + " (" + obj.getString("symbol") + ")";
                    String a = obj.getString("definition");
                    if (obj.has("formula")) a += "\nFormula: " + obj.getString("formula");
                    flashcardDao.insert(new Flashcard(q, a, categoryId));
                }
            }
            if (unitsObj.has("kinetic_energy")) {
                JSONObject ke = unitsObj.getJSONObject("kinetic_energy");
                String q = "الطاقة الحركية (Kinetic Energy)";
                String a = ke.getString("definition") + "\nFormula: " + ke.getString("formula");
                flashcardDao.insert(new Flashcard(q, a, categoryId));
            }
        }

        // 5. Thermal Concepts
        if (chapter.has("thermal_concepts")) {
            JSONArray concepts = chapter.getJSONArray("thermal_concepts");
            for (int i = 0; i < concepts.length(); i++) {
                JSONObject obj = concepts.getJSONObject(i);
                String q = obj.getString("term");
                String a = obj.getString("definition") + "\nUnit: " + obj.getString("unit");
                flashcardDao.insert(new Flashcard(q, a, categoryId));
            }
        }

        // 6. Thermodynamic Terms
        if (chapter.has("thermodynamic_terms")) {
            JSONObject terms = chapter.getJSONObject("thermodynamic_terms");
            if (terms.has("system_components")) {
                JSONArray comps = terms.getJSONArray("system_components");
                for (int i = 0; i < comps.length(); i++) {
                    JSONObject obj = comps.getJSONObject(i);
                    String q = obj.getString("term");
                    String a = obj.has("definition") ? obj.getString("definition") : obj.getString("formula");
                    flashcardDao.insert(new Flashcard(q, a, categoryId));
                }
            }
            if (terms.has("system_types")) {
                JSONArray stypes = terms.getJSONArray("system_types");
                for (int i = 0; i < stypes.length(); i++) {
                    JSONObject obj = stypes.getJSONObject(i);
                    String q = obj.getString("type");
                    String a = obj.getString("definition") + "\nExample: " + obj.getString("example");
                    flashcardDao.insert(new Flashcard(q, a, categoryId));
                }
            }
        }

        // 7. State and Path Functions
        if (chapter.has("state_and_path_functions")) {
            JSONArray funcs = chapter.getJSONArray("state_and_path_functions");
            for (int i = 0; i < funcs.length(); i++) {
                JSONObject obj = funcs.getJSONObject(i);
                flashcardDao.insert(new Flashcard(obj.getString("type"), obj.getString("definition"), categoryId));
            }
        }

        // 8. General Properties of Matter
        if (chapter.has("general_properties_of_matter")) {
            JSONArray props = chapter.getJSONArray("general_properties_of_matter");
            for (int i = 0; i < props.length(); i++) {
                JSONObject obj = props.getJSONObject(i);
                flashcardDao.insert(new Flashcard(obj.getString("category"), obj.getString("definition"), categoryId));
            }
        }

        // 9. Enthalpy and Calorimeter
        if (chapter.has("enthalpy_and_calorimeter")) {
            JSONObject ec = chapter.getJSONObject("enthalpy_and_calorimeter");
            if (ec.has("enthalpy_of_reaction")) {
                JSONObject er = ec.getJSONObject("enthalpy_of_reaction");
                flashcardDao.insert(new Flashcard(er.getString("symbol"), er.getString("definition"), categoryId));
            }
            if (ec.has("calorimeter")) {
                JSONObject cal = ec.getJSONObject("calorimeter");
                flashcardDao.insert(new Flashcard(cal.getString("question"), cal.getString("answer"), categoryId));
            }
        }
    }
}
