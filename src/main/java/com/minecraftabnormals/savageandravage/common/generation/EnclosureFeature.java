package com.minecraftabnormals.savageandravage.common.generation;

import com.minecraftabnormals.savageandravage.common.entity.CreepieEntity;
import com.minecraftabnormals.savageandravage.common.entity.GrieferEntity;
import com.minecraftabnormals.savageandravage.core.registry.SRBlocks;
import com.minecraftabnormals.savageandravage.core.registry.SREntities;
import com.mojang.serialization.Codec;

import javafx.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.Random;

public class EnclosureFeature extends Feature<NoFeatureConfig> {
    private static final Direction[] horizontalDirections = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private final boolean shouldCreateWoolMarkers = false; //debug feature, remove on actual release

    public EnclosureFeature(Codec<NoFeatureConfig> featureConfigCodec) {
        super(featureConfigCodec);
    }

    //Should be split into methods, but the weird chunk out of bounds problem prevents that
    @Override
    public boolean generate(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos centerPos, NoFeatureConfig config) {
        //Randomising the centre position for variety
        int minY = centerPos.getY()-(4+rand.nextInt(2)); //the lowest y to use when making the pit
        centerPos = findSuitablePosition(reader, centerPos, minY, rand);
        if (centerPos == null) return false;
        //(These position arrays use the surface as their Y level - the air block above the ground)
        ArrayList<BlockPos> holePositions = new ArrayList<>(); //the positions for the hole - i.e. which x and z values are to be cut out
        ArrayList<BlockPos> edgePositions = new ArrayList<>(); //positions at the edge of the hole, where 'drop-offs' might be placed
        //Adding the 'starting positions' - the center and its neighbours
        holePositions.add(centerPos);
        for (Direction dir : horizontalDirections) {
            edgePositions.add(centerPos.offset(dir));
        }
        for (int i = 0; i <= 5+rand.nextInt(5); i++) { //Iterations randomised for variety
            edgePositions = expandHole(edgePositions, holePositions, centerPos, reader, rand);
        }
        //Positions for fences to be placed on or griefers to spawn on
        ArrayList<BlockPos> outlinePositions = processEdges(edgePositions, holePositions, reader, rand);
        generateHole(holePositions, minY, reader, rand);
        //New array set because generateFences removes positions from outlinePositions
        ArrayList<BlockPos> nonFenceOutlines = generateFences(outlinePositions, reader, rand);
        for (BlockPos outlinePos : outlinePositions) {
            reader.getBlockState(outlinePos).updateNeighbours(reader, outlinePos, 3); //fixes fence connections
        }
        GrieferEntity griefer = SREntities.GRIEFER.get().create(reader.getWorld());
        if (griefer != null && !nonFenceOutlines.isEmpty()) {
            BlockPos grieferPos = nonFenceOutlines.get(rand.nextInt(nonFenceOutlines.size())).toMutable(); //put the griefer at a random location on the edge of the hole
            griefer.setLocationAndAngles(grieferPos.getX(), grieferPos.getY(), grieferPos.getZ(), 0, 0);
            griefer.onInitialSpawn(reader, reader.getDifficultyForLocation(grieferPos), SpawnReason.CHUNK_GENERATION, null, null);
            reader.addEntity(griefer);
        }
        //Placing the decorations (disabled because broken)
        /*ArrayList<Pair<Direction, BlockPos>> potentialDecorationStarts = getDecorationStarts(edgePositions, allOutlinePositions, holePositions, reader);
        generateDecorations(potentialDecorationStarts, reader, rand);*/
        return true;
    }

    /**
     * For the area 3 blocks around the center position, checks if the area 5 blocks around it are suitable, returning
     * one of these valid positions, or null if none are valid.
     * */
    private BlockPos findSuitablePosition(ISeedReader reader, BlockPos centerPos, int minY, Random rand) {
        BlockPos.Mutable pos = centerPos.toMutable();
        ArrayList<BlockPos> suitablePositions = new ArrayList<>();
        for(int bigX=centerPos.getX()-3; bigX<centerPos.getX()+3; bigX++) {
            for(int bigZ=centerPos.getZ()-3; bigZ<centerPos.getZ()+3; bigZ++) {
                boolean areaClear = true;
                for (int x=bigX-4; x<=bigX+4; x++) {
                    for (int z = bigZ - 4; z <= bigZ + 4; z++) {
                        for (int y = minY; y <= centerPos.getY() + 1; y++) {
                            pos.setPos(x, y, z);
                            /*isOpaqueCube should return false for surface positions (where y>=centerPos.getY() is true)
                            but true for underground positions (where y>=centerPos.getY() is false), so this has the effect
                            of checking that the area is 'clear'
                            */
                            if ((y >= centerPos.getY()) == reader.getBlockState(pos).isOpaqueCube(reader, pos)) {
                                areaClear = false;
                            }
                        }
                    }
                }
                if (areaClear) {
                    pos.setPos(bigX, centerPos.getY(), bigZ);
                    suitablePositions.add(pos);
                }
            }
        }
        return !suitablePositions.isEmpty() ? suitablePositions.get(rand.nextInt(suitablePositions.size())) : null;
    }

    /**
     * Like isAreaClear, but it only checks one position and two y levels - ground and the block above
     * */
    private boolean isSurfacePositionClear(ISeedReader reader, BlockPos pos) {
        if (!(reader.getBlockState(pos).isOpaqueCube(reader, pos))) {
            return reader.getBlockState(pos.offset(Direction.DOWN)).isOpaqueCube(reader, pos.offset(Direction.DOWN));
        }
        return false;
    }

    /**
     * Takes in an arraylist of the positions at the edge of a hole and makes it bigger. Also increases the size of the holePositions
     * */
    private ArrayList<BlockPos> expandHole(ArrayList<BlockPos> edgePositions, ArrayList<BlockPos> holePositions, BlockPos centerPos, ISeedReader reader, Random rand) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable();
        ArrayList<BlockPos> newEdgePositions = new ArrayList<>(edgePositions); //caching edgePositions as elements need to be removed
        for (BlockPos edgePos : edgePositions) {
            //This makes it less likely to expand further as it gets larger, preventing ridiculous hole sizes
            if (rand.nextInt(49) > edgePos.distanceSq(centerPos)) {
                ArrayList<BlockPos> validPotentialPositions = new ArrayList<>();
                boolean hasHoleNeighbor = false;
                boolean isNothingBlocking = true;
                for (Direction dir : horizontalDirections) {
                    currentPos.setPos(edgePos.offset(dir).toMutable());
                    if (!holePositions.contains(currentPos)) {
                        if (!edgePositions.contains(currentPos)) {
                            if(isSurfacePositionClear(reader, currentPos)) {
                                validPotentialPositions.add(currentPos.toMutable());
                            } else {
                                isNothingBlocking = false;
                                break;
                            }
                        }
                    } else hasHoleNeighbor = true;
                }
                if (isNothingBlocking) {
                    if (hasHoleNeighbor) {
                        if (validPotentialPositions.size() == 0) {
                            holePositions.add(edgePos); //Prevents pillars in the pit
                            newEdgePositions.remove(edgePos);
                        } else {
                            /*This random check makes it less likely that positions with fewer neighbours will expand further -
                            in theory, this prevents straight lines in one direction from going on too far */
                            if ((rand.nextFloat() < (1 / (validPotentialPositions.size() + 1.0f)))) {
                                newEdgePositions.addAll(validPotentialPositions);
                                newEdgePositions.remove(edgePos);
                                holePositions.add(edgePos);
                            }
                        }

                    } else newEdgePositions.remove(edgePos);
                }
            }
        }
        return newEdgePositions;
    }

    /**
     * Generates the blocks at drop off positions and places their outlines into an array which is returned
     * */
    private ArrayList<BlockPos> processEdges(ArrayList<BlockPos> edgePositions, ArrayList<BlockPos> holePositions, ISeedReader reader, Random rand) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable();
        ArrayList<BlockPos> outlinePositions = new ArrayList<>();
        for (BlockPos edgePos : edgePositions) {
            //The rotate y stuff is used to get diagonal neighbours, is there a better way?
            for (Direction dir : horizontalDirections) {
                currentPos.setPos(edgePos.offset(dir));
                if(!edgePositions.contains(currentPos) && !holePositions.contains(currentPos)) {
                    outlinePositions.add(currentPos.toImmutable()); //Needs to be cloned
                }
                currentPos.setPos(currentPos.offset(dir.rotateY()));
                if(!edgePositions.contains(currentPos) && !holePositions.contains(currentPos)) {
                    outlinePositions.add(currentPos.toImmutable());
                }
            }
            if(rand.nextFloat()<0.6f) { //Randomised to make the hole look a bit more natural
                reader.setBlockState(edgePos.offset(Direction.DOWN), Blocks.AIR.getDefaultState(), 3);
            }
            if (shouldCreateWoolMarkers) reader.setBlockState(edgePos.offset(Direction.UP, 3), Blocks.RED_WOOL.getDefaultState(), 3); //test to show where posses are
        }
        return outlinePositions;
    }

    /**
     * Takes in an arraylist of hole positions and generates the hole from them, including mobs inside
     * */
    private void generateHole(ArrayList<BlockPos> holePositions, int minY, ISeedReader reader, Random rand) {
        for (BlockPos holePos : holePositions) {
            if (shouldCreateWoolMarkers) reader.setBlockState(holePos.offset(Direction.UP, 3), Blocks.LIGHT_BLUE_WOOL.getDefaultState(), 3); //test to show where posses are
            BlockPos.Mutable currentPos = new BlockPos.Mutable();
            currentPos.setPos(holePos);
            for(int i=minY; i<holePos.getY(); i++) { //holePos.getY() is the block 1 above the surface, so < is used
                currentPos.setY(i);
                if (i == minY) {
                    reader.setBlockState(currentPos, Blocks.COARSE_DIRT.getDefaultState(), 3);
                } else if (i == minY + 1) {
                    reader.setBlockState(currentPos, rand.nextFloat() < 0.4f ? rand.nextFloat() > 0.3f ? Blocks.GRASS.getDefaultState() : Blocks.DEAD_BUSH.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
                } else {
                    reader.setBlockState(currentPos, Blocks.AIR.getDefaultState(), 3);
                }
            }
            if(rand.nextFloat()<0.3f) {
                MobEntity entity = rand.nextFloat() < 0.5f ? EntityType.CREEPER.create(reader.getWorld()) : SREntities.CREEPIE.get().create(reader.getWorld());
                if (entity != null) {
                    entity.setLocationAndAngles(currentPos.getX() + 0.5, minY + 1, currentPos.getZ() + 0.5, 0, 0);
                    entity.onInitialSpawn(reader, reader.getDifficultyForLocation(currentPos), SpawnReason.CHUNK_GENERATION, null, null);
                    if (entity instanceof CreepieEntity) {
                        ((CreepieEntity) entity).attackPlayersOnly = true;
                    }
                    reader.addEntity(entity);
                }
            }
        }
    }

    /**
     * Takes in an arraylist of positions of the outline around the hole, and generates fences at them.
     * Returns the outlines excluding fence positions
     * */
    private ArrayList<BlockPos> generateFences(ArrayList<BlockPos> outlinePositions, ISeedReader reader, Random rand) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable();
        ArrayList<BlockPos> nonFenceOutlines = new ArrayList<>(outlinePositions);
        if (!nonFenceOutlines.isEmpty()) {
            for (BlockPos firstFencePos : outlinePositions) {
                if (shouldCreateWoolMarkers) reader.setBlockState(firstFencePos.offset(Direction.UP, 3), Blocks.MAGENTA_WOOL.getDefaultState(), 3); //test to show where posses are
                if (rand.nextFloat() < 0.2f) {
                    for (Direction dir : horizontalDirections) {
                        currentPos.setPos(firstFencePos.offset(dir));
                        if (nonFenceOutlines.contains(currentPos) && reader.getBlockState(currentPos.offset(Direction.DOWN)).isOpaqueCube(reader, currentPos.offset(Direction.DOWN))) {
                            reader.setBlockState(firstFencePos, Blocks.SPRUCE_FENCE.getDefaultState(), 3);
                            reader.setBlockState(currentPos, Blocks.SPRUCE_FENCE.getDefaultState(), 3);
                            //Removed so that the griefer doesn't spawn in a fence. Also prevents redundant placement.
                            nonFenceOutlines.remove(firstFencePos);
                            nonFenceOutlines.remove(currentPos);
                            break;
                        }
                    }
                }
            }
        }
        return nonFenceOutlines;
    }

    /**
     * Finds valid positions to place decorations from
     * */
    //TODO work out a distance check to prevent intersection, also the fuck is going on with the direction?
    private ArrayList<Pair<Direction, BlockPos>> getDecorationStarts(ArrayList<BlockPos> edgePositions, ArrayList<BlockPos> fencePositions, ArrayList<BlockPos> holePositions, ISeedReader reader) {
        //collecting available decoration positions. might break
        BlockPos.Mutable currentPos = new BlockPos.Mutable();
        ArrayList<Pair<Direction, BlockPos>> decorationStarts = new ArrayList<>();
        for (BlockPos edgePos : edgePositions) {
            for (Direction dir : horizontalDirections) { //checks area beyond the fence to see if it is empty
                currentPos.setPos(edgePos.offset(dir, 4));
                if (!fencePositions.contains(currentPos) && !edgePositions.contains(currentPos) && !holePositions.contains(currentPos)) {
                    boolean isClear = true;
                    //Directional offset might be buggy
                    int minX = currentPos.offset(dir.rotateY(), 2).offset(dir.getOpposite()).getX();
                    int minZ = currentPos.offset(dir.rotateY(), 2).offset(dir.getOpposite()).getZ();
                    currentPos.setPos(currentPos.offset(dir, 4).offset(dir.rotateYCCW(), 2));
                    int maxX = currentPos.getX();
                    int maxZ = currentPos.getZ();
                    for (int x = minX; x <= maxX && isClear; x++) {
                        for (int z = minZ; z <= maxZ && isClear; z++) {
                            for (int y = edgePos.getY() - 1; y <= edgePos.getY() + 1; y++) {
                                currentPos.setPos(x, y, z);
                                if (((y >= edgePos.getY()) == reader.getBlockState(currentPos).isOpaqueCube(reader, currentPos)) || fencePositions.contains(currentPos) || edgePositions.contains(currentPos) || holePositions.contains(currentPos)) {
                                    isClear = false;
                                }
                            }
                        }
                    }
                    if (isClear) {
                        currentPos.setPos(edgePos.offset(dir, 4));
                        decorationStarts.add(new Pair<>(dir, currentPos));
                        if (shouldCreateWoolMarkers)
                            reader.setBlockState(currentPos.offset(Direction.UP, 4), Blocks.ORANGE_WOOL.getDefaultState(), 3); //test !
                    }
                }
            }
        }
        return decorationStarts;
    }


    /**
     * Chooses positions from a potential decoration positions list and generates decorations
     * */
    //TODO make these actually place at the right positions in the array
    private void generateDecorations(ArrayList<Pair<Direction, BlockPos>> potentialStarts, ISeedReader reader, Random rand) {
        BlockPos.Mutable currentPos = new BlockPos.Mutable();
        if (!potentialStarts.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                Pair<Direction, BlockPos> positionInfo = potentialStarts.get((potentialStarts.size()/4)*(i+1)); //ideally makes the positions different enough
                currentPos.setPos(positionInfo.getValue());
                Direction dir = positionInfo.getKey();
                BlockPos[][] decorationPositions = new BlockPos[5][5]; //first bracket for along, second for ahead
                //Caching decoration locations to make it easier to set blockstates
                for (int j = 0; j < 5; j++) {
                    for (int k = 0; k < 5; k++) {
                        if (k<2) {
                            decorationPositions[k][j] =  currentPos.offset(dir.rotateYCCW(), 2 - k);
                        } else if (k==2) {
                            decorationPositions[k][j] = currentPos;
                        } else {
                            decorationPositions[k][j] = currentPos.offset(dir.rotateY(), k - 2);
                        }
                        if (shouldCreateWoolMarkers) {
                            reader.setBlockState(decorationPositions[k][j].offset(Direction.UP, 5), k==2 && j==0? Blocks.CYAN_WOOL.getDefaultState() : Blocks.BLUE_WOOL.getDefaultState(), 3); //test !
                        }
                    }
                    currentPos.setPos(currentPos.offset(dir));
                }
                //Placing the decorations
                if (i < 2) {
                    switch (rand.nextInt(3)) {
                        case 0:
                            //big cage with spore bomb
                            for (int j = 0; j < 6; j++) {
                                if (j == 0 || j == 4) {
                                    for (int k = 0; k < 5; k++) {
                                        reader.setBlockState(decorationPositions[k][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                        reader.setBlockState(decorationPositions[k][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                        if (j == 4) {
                                            reader.setBlockState(decorationPositions[2][j], Blocks.OAK_PLANKS.getDefaultState(), 3);
                                        }
                                    }
                                } else if (j == 5) {
                                        reader.setBlockState(decorationPositions[2][4].offset(dir), Blocks.OAK_BUTTON.getDefaultState().with(HorizontalFaceBlock.HORIZONTAL_FACING, dir), 3);
                                } else {
                                    reader.setBlockState(decorationPositions[0][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[0][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[4][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[4][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                    if (j == 3) {
                                        reader.setBlockState(decorationPositions[2][j], SRBlocks.SPORE_BOMB.get().getDefaultState(), 3);
                                    }
                                }
                            }
                            break;
                        case 1:
                            //medium cage;
                            for (int j = 0; j < 4; j++) {
                                if (j > 0 && j < 3) {
                                    reader.setBlockState(decorationPositions[1][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[1][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[4][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[4][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                    CreeperEntity creeper = EntityType.CREEPER.create(reader.getWorld());
                                    if (creeper != null) {
                                        currentPos.setPos(decorationPositions[2 + rand.nextInt(2)][j]);
                                        creeper.setLocationAndAngles(currentPos.getX(), currentPos.getY(), currentPos.getZ(), 0, 0);
                                        creeper.onInitialSpawn(reader, reader.getDifficultyForLocation(currentPos), SpawnReason.CHUNK_GENERATION, null, null);
                                        reader.addEntity(creeper);
                                    }
                                } else {
                                    for (int k = 1; k < 5; k++) {
                                        reader.setBlockState(decorationPositions[k][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                        reader.setBlockState(decorationPositions[k][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                    }
                                }
                            }
                            break;
                        case 2:
                            for (int j = 0; j < 3; j++) {
                                if (j == 1) {
                                    reader.setBlockState(decorationPositions[1][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[1][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[3][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                    reader.setBlockState(decorationPositions[3][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                } else {
                                    for (int k = 1; k < 4; k++) {
                                        reader.setBlockState(decorationPositions[k][j], Blocks.OAK_FENCE.getDefaultState(), 3);
                                        reader.setBlockState(decorationPositions[k][j].offset(Direction.UP), Blocks.OAK_FENCE.getDefaultState(), 3);
                                    }
                                }
                            }
                    }
                } else {
                    switch (rand.nextInt(ModList.get().isLoaded("quark") ? 3 : 2)) { //Only uses creeper spore sack decoration if quark is loaeded
                        case 0:
                            //crafting tables
                            break;
                        case 1:
                            //blast proof plates
                            break;
                        case 2:
                            //creeper spore sack

                    }
                }
                for (BlockPos[] positions : decorationPositions) {
                    for (BlockPos position : positions) {
                        reader.getBlockState(position).updateNeighbours(reader, position, 3); //fixes fence connections
                        reader.getBlockState(position.offset(Direction.UP)).updateNeighbours(reader, position.offset(Direction.UP), 3); //fixes fence connections
                    }
                }
            }
        }
    }
 }