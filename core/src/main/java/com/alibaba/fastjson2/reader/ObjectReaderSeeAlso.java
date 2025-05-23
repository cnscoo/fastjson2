package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

final class ObjectReaderSeeAlso<T>
        extends ObjectReaderAdapter<T> {
    ObjectReaderSeeAlso(
            Class objectType,
            Supplier<T> defaultCreator,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            Class seeAlsoDefault,
            FieldReader... fieldReaders
    ) {
        super(
                objectType,
                typeKey,
                null,
                JSONReader.Feature.SupportAutoType.mask,
                null,
                defaultCreator,
                null,
                seeAlso,
                seeAlsoNames,
                seeAlsoDefault,
                fieldReaders
        );
    }

    ObjectReaderSeeAlso addSubType(Class subTypeClass, String subTypeClassName) {
        for (int i = 0; i < seeAlso.length; i++) {
            if (seeAlso[i] == subTypeClass) {
                return this;
            }
        }

        Class[] seeAlso1 = Arrays.copyOf(seeAlso, seeAlso.length + 1);
        String[] seeAlsoNames1 = Arrays.copyOf(this.seeAlsoNames, this.seeAlsoNames.length + 1);
        seeAlso1[seeAlso1.length - 1] = subTypeClass;
        if (subTypeClassName == null) {
            JSONType jsonType = (JSONType) subTypeClass.getAnnotation(JSONType.class);
            if (jsonType != null) {
                subTypeClassName = jsonType.typeName();
            }
        }
        if (subTypeClassName != null) {
            seeAlsoNames1[seeAlsoNames1.length - 1] = subTypeClassName;
        }

        return new ObjectReaderSeeAlso(
                objectClass,
                creator,
                typeKey,
                seeAlso1,
                seeAlsoNames1,
                seeAlsoDefault,
                fieldReaders
        );
    }

    @Override
    public T createInstance(long features) {
        if (creator == null) {
            return null;
        }
        return creator.get();
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.isArray()) {
            if (jsonReader.isSupportBeanArray()) {
                return readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
            } else {
                throw new JSONException(jsonReader.info("expect object, but " + JSONB.typeName(jsonReader.getType())));
            }
        }

        JSONReader.SavePoint savePoint = jsonReader.mark();
        jsonReader.nextIfObjectStart();

        T object = null;
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long hash = jsonReader.readFieldNameHashCode();
            if (hash == typeKeyHashCode) {
                long typeHash = jsonReader.readValueHashCode();
                JSONReader.Context context = jsonReader.getContext();
                ObjectReader autoTypeObjectReader = autoType(context, typeHash);
                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                    if (autoTypeObjectReader == null) {
                        throw new JSONException(jsonReader.info("autoType not support : " + typeName));
                    }
                }

                if (autoTypeObjectReader == this) {
                    continue;
                }
                if (i != 0) {
                    jsonReader.reset(savePoint);
                }

                jsonReader.setTypeRedirect(true);
                return (T) autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
            }

            if (hash == 0) {
                continue;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | this.features)) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                fieldReader = getFieldReaderLCase(nameHashCodeLCase);
            }
            if (fieldReader == null) {
                processExtra(jsonReader, object);
                continue;
            }

            if (object == null) {
                object = createInstance(jsonReader.getContext().getFeatures() | features);
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        if (object == null) {
            object = createInstance(jsonReader.getContext().getFeatures() | features);
        }

        if (schema != null) {
            schema.assertValidate(object);
        }

        return object;
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.jsonb) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfComma();
            return null;
        }

        if (jsonReader.isString()) {
            long valueHashCode = jsonReader.readValueHashCode();

            for (int i = 0; i < seeAlso.length; i++) {
                Class seeAlsoType = seeAlso[i];
                if (Enum.class.isAssignableFrom(seeAlsoType)) {
                    ObjectReader seeAlsoTypeReader = jsonReader.getObjectReader(seeAlsoType);

                    Enum e = null;
                    if (seeAlsoTypeReader instanceof ObjectReaderImplEnum) {
                        e = ((ObjectReaderImplEnum) seeAlsoTypeReader).getEnumByHashCode(valueHashCode);
                    }

                    if (e != null) {
                        return (T) e;
                    }
                }
            }

            String strVal = jsonReader.getString();
            throw new JSONException(jsonReader.info("not support input " + strVal));
        }

        JSONReader.SavePoint savePoint = jsonReader.mark();

        long featuresAll = jsonReader.features(this.getFeatures() | features);
        if (jsonReader.isArray()) {
            if ((featuresAll & JSONReader.Feature.SupportArrayToBean.mask) != 0) {
                return readArrayMappingObject(jsonReader, fieldType, fieldName, features);
            }

            return processObjectInputSingleItemArray(jsonReader, fieldType, fieldName, featuresAll);
        }

        T object = null;
        boolean objectStart = jsonReader.nextIfObjectStart();
        if (!objectStart) {
            char ch = jsonReader.current();
            // skip for fastjson 1.x compatible
            if (ch == 't' || ch == 'f') {
                jsonReader.readBoolValue(); // skip
                return null;
            }

            if (ch != '"' && ch != '\'' && ch != '}') {
                throw new JSONException(jsonReader.info());
            }
        }

        Map<Long, Object> fieldValues = null;
        for (int i = 0; ; i++) {
            if (jsonReader.nextIfObjectEnd()) {
                if (object == null) {
                    object = createInstance(jsonReader.getContext().getFeatures() | features);
                }
                break;
            }

            JSONReader.Context context = jsonReader.getContext();
            long features3, hash = jsonReader.readFieldNameHashCode();
            JSONReader.AutoTypeBeforeHandler autoTypeFilter = context.getContextAutoTypeBeforeHandler();
            if ((hash == getTypeKeyHash() || (seeAlsoDefault != null && seeAlsoDefault != Void.class))
                    && ((((features3 = (features | getFeatures() | context.getFeatures())) & JSONReader.Feature.SupportAutoType.mask) != 0) || autoTypeFilter != null)
            ) {
                ObjectReader reader = null;
                long typeHash = jsonReader.readTypeHashCode();

                Number typeNumber = null;
                String typeNumberStr = null;
                if (typeHash == -1 && jsonReader.isNumber()) {
                    typeNumber = jsonReader.readNumber();
                    typeNumberStr = typeNumber.toString();
                    typeHash = Fnv.hashCode64(typeNumberStr);
                }
                if (autoTypeFilter != null) {
                    Class<?> filterClass = autoTypeFilter.apply(typeHash, objectClass, features3);
                    if (filterClass == null) {
                        filterClass = autoTypeFilter.apply(jsonReader.getString(), objectClass, features3);
                        if (filterClass != null) {
                            reader = context.getObjectReader(filterClass);
                        }
                    }
                }

                String typeName = null;
                if (reader == null) {
                    reader = autoType(context, typeHash);
                    if (reader != null && hash != HASH_TYPE) {
                        typeName = jsonReader.getString();
                    }
                }

                if (reader == null) {
                    typeName = jsonReader.getString();
                    reader = context.getObjectReaderAutoType(
                            typeName, objectClass, features3
                    );

                    if (reader == null && seeAlsoDefault != null) {
                        reader = context.getObjectReader(seeAlsoDefault);
                    }

                    if (reader == null) {
                        throw new JSONException(jsonReader.info("No suitable ObjectReader found for" + typeName));
                    }
                }

                if (reader == this) {
                    continue;
                }

                FieldReader fieldReader = reader.getFieldReader(hash);
                if (fieldReader == null && hash != HASH_TYPE) {
                    fieldReader = reader.getFieldReader(typeKey);
                }
                if (fieldReader != null && typeName == null) {
                    if (typeNumberStr != null) {
                        typeName = typeNumberStr;
                    } else {
                        typeName = jsonReader.getString();
                    }
                }

                if (i != 0 || fieldReader != null) {
                    jsonReader.reset(savePoint);
                }

                object = (T) reader.readObject(
                        jsonReader, fieldType, fieldName, features | getFeatures()
                );

                if (fieldReader != null) {
                    if (typeNumber != null) {
                        fieldReader.accept(object, typeNumber);
                    } else {
                        fieldReader.accept(object, typeName);
                    }
                }

                return object;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | getFeatures())) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                fieldReader = getFieldReaderLCase(nameHashCodeLCase);
            }

            if (object == null) {
                object = createInstance(jsonReader.getContext().getFeatures() | features);
            }

            if (fieldReader == null) {
                processExtra(jsonReader, object);
                continue;
            }

            if (object == null) {
                Object fieldValue = fieldReader.readFieldValue(jsonReader);
                if (fieldValues == null) {
                    fieldValues = new LinkedHashMap<>();
                }
                fieldValues.put(hash, fieldValue);
            } else {
                fieldReader.readFieldValue(jsonReader, object);
            }
        }

        if (fieldValues != null) {
            for (Map.Entry<Long, Object> entry : fieldValues.entrySet()) {
                FieldReader fieldReader = getFieldReader(entry.getKey());
                fieldReader.accept(object, entry.getValue());
            }
        }

        jsonReader.nextIfComma();

        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            object = (T) buildFunction.apply(object);
        }

        if (schema != null) {
            schema.assertValidate(object);
        }

        return object;
    }
}
